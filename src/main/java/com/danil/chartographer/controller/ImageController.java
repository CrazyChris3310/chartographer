package com.danil.chartographer.controller;

import com.danil.chartographer.domain.Response;
import com.danil.chartographer.exception.NoSuchIdException;
import com.danil.chartographer.exception.SizeException;
import com.danil.chartographer.service.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.BufferedImage;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/chartas")
public class ImageController {

  private final ImageService imageService;

  public ImageController(ImageService imageService) {
    this.imageService = imageService;
  }

  /**
   * Creates a new black bmp image in the folder with given {@code width} and {@code height}
   *
   * @return id of newly created image
   * @throws IOException if an error occurs during writing
   */
  @PostMapping()
  @ResponseStatus(HttpStatus.CREATED)
  public String createChart(@RequestParam int width, @RequestParam int height) throws IOException {
    if (width > 20000 || height > 50000) {
      throw new SizeException("Sizes of charta are too big");
    }
    return imageService.create(width, height);
  }

  /**
   * Receives an image fragment and adds it to an image with given id on the place according to
   * coordinates
   *
   * @param id     id of an image to which fragment should be added
   * @param x      horizontal coordinate to where to put fragment
   * @param y      vertical coordinate to where to put fragment
   * @param width  fragment's width
   * @param height fragment's height
   * @param data   fragment represented in bytes
   * @throws IOException if an error occurs during reading or writing
   */
  @PostMapping("/{id}")
  public void saveFragment(@PathVariable String id,
                           @RequestParam int x,
                           @RequestParam int y,
                           @RequestParam int width,
                           @RequestParam int height,
                           @RequestBody byte[] data) throws IOException {

    BufferedImage chartas = imageService.getImage(id);

    if (x >= chartas.getWidth() || x < 0 || y < 0 || y >= chartas.getHeight()) {
      throw new SizeException("Coordinates of fragment are beyond the charta borders");
    }
    if (width < 0 || height < 0) {
      throw new SizeException("Sizes of fragment are invalid");
    }

    BufferedImage fragment = imageService.decryptImage(data);
    if (width != fragment.getWidth() || height != fragment.getHeight()) {
      throw new SizeException("Provided sizes do not match sizes of fragment");
    }

    imageService.copy(0, 0, x, y, width, height, fragment, chartas);
    imageService.save(chartas, id);

  }

  /**
   * Returns a fragment with given size from an image with given id starting at (x, y). If sizes are
   * too big, then the largest possible fragment that fits sizes is returned.
   *
   * @param id     id of an image from where to take fragment
   * @param x      horizontal coordinate where the fragment begins
   * @param y      vertical coordinate where the fragment begins
   * @param width  fragment's width
   * @param height fragment's height
   * @return image fragment
   * @throws IOException if an error occurs during reading or writing
   */
  @GetMapping(value = "/{id}", produces = "image/bmp")
  public byte[] getFragment(@PathVariable String id,
                            @RequestParam int x,
                            @RequestParam int y,
                            @RequestParam int width,
                            @RequestParam int height) throws IOException {

    if (width > 5000 || height > 5000 || width < 0 || height < 0) {
      throw new SizeException("Sizes of charta are too big");
    }

    BufferedImage chartas = imageService.getImage(id);

    BufferedImage result = new BufferedImage(width, height, chartas.getType());

    imageService.copy(x, y, 0, 0, width, height, chartas, result);

    return imageService.encryptImage(result);
  }

  /**
   * Removes an image with given id from folder
   *
   * @param id id of an image to remove
   * @throws IOException if an I/O error occurs
   */
  @DeleteMapping("/{id}")
  public void removeChart(@PathVariable String id) throws IOException {
    imageService.removeImage(id);
  }

  @ExceptionHandler(SizeException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Response handleSizeException(SizeException e) {
    log.info(e.getMessage());
    return new Response(e.getMessage());
  }


  @ExceptionHandler(NoSuchIdException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Response handleIdException(NoSuchIdException e) {
    log.info(e.getMessage());
    return new Response(e.getMessage());
  }

  @ExceptionHandler(IOException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Response handleIOException(IOException e) {
    log.error(e.getMessage());
    return new Response(e.getMessage());
  }

}
