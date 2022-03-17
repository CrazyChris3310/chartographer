package com.danil.chartographer.controller;

import com.danil.chartographer.domain.Response;
import com.danil.chartographer.exception.NoSuchIdException;
import com.danil.chartographer.exception.SizeException;
import com.danil.chartographer.service.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public String createChart(@RequestParam int width, @RequestParam int height) throws IOException {
        if (width > 20000 || height > 50000)
            throw new SizeException("Sizes of charta are too big");
        return imageService.create(width, height);
    }

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

    @DeleteMapping("/{id}")
    public void removeChart(@PathVariable String id) throws IOException {
        imageService.removeImage(id);
    }

    // FIXME: code looks very alike in exceptions handlers

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
