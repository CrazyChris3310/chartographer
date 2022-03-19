package com.danil.chartographer.service;

import com.danil.chartographer.exception.NoSuchIdException;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

/**
 * ImageService is a class for working with {@link BufferedImage} images. It is used for their
 * creation, updating, reading and removing.
 */
@Service
public class ImageService {

  private final String pathForSaving;

  /**
   * Takes command line arguments and finds there a folder for saving images
   *
   * @param args command line arguments
   */
  public ImageService(ApplicationArguments args) {
    List<String> files = args.getNonOptionArgs();
    pathForSaving = files.get(0);
  }

  /**
   * Creates an image with given width and height and returns an id of that image. If folder where
   * images should be stored doesn't exist, it creates it.
   *
   * @return id of created image
   * @throws IOException if an error occurs during writing
   */
  public String create(int width, int height) throws IOException {
    File uploadDir = new File(pathForSaving);
    if (!uploadDir.exists()) {
      boolean created = uploadDir.mkdirs();
      if (!created) throw new IOException("Unable to create directories for saving images");
    }

    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
    String uuid = UUID.randomUUID().toString();

    ImageIO.write(image, "bmp", new File(pathForSaving + "/" + uuid + ".bmp"));

    return uuid;
  }

  /**
   * Copies {@code width * height} pixels from source {@link BufferedImage} starting at point (x0,
   * y0) to destination {@link BufferedImage} starting at point (x1, y1)
   *
   * @param x0          horizontal coordinate from where to start copying
   * @param y0          vertical coordinate from where to start copying
   * @param x1          horizontal coordinate to where to start copying
   * @param y1          vertical coordinate to where to start copying
   * @param source      image from where content is copied
   * @param destination image to which content is being copied
   */
  public void copy(int x0, int y0, int x1, int y1, int width, int height, BufferedImage source,
                   BufferedImage destination) {
    int realWidth = Math.min(Math.min(source.getWidth() - x0, width), destination.getWidth() - x1);
    int realHeight = Math.min(Math.min(source.getHeight() - y0, height),
                              destination.getHeight() - y1);

    for (int i = 0; i < realWidth; ++i) {
      for (int j = 0; j < realHeight; ++j) {
        destination.setRGB(x1 + i, y1 + j, source.getRGB(x0 + i, y0 + j));
      }
    }
  }

  /**
   * Saves image with given name
   *
   * @param image - image to save
   * @param name  - id of the image
   * @throws IOException if an error occurs during writing
   */
  public void save(BufferedImage image, String name) throws IOException {
    ImageIO.write(image, "bmp", new File(pathForSaving + "/" + name + ".bmp"));
  }

  /**
   * Returns {@link BufferedImage} object by it's id
   *
   * @return image object
   * @throws IOException if an error occurs during reading
   */
  public BufferedImage getImage(String id) throws IOException {
    File file = getImageFile(id);
    return ImageIO.read(file);
  }

  /**
   * Get file with image by it's id
   *
   * @param id id of the image
   * @return file that contains image
   */
  private File getImageFile(String id) {
    File file = new File(pathForSaving + "/" + id + ".bmp");
    if (!file.exists()) {
      throw new NoSuchIdException("No charta with id " + id + " can be found");
    }

    return file;
  }

  /**
   * Removes an image from file by given id
   *
   * @param id id of the image to remove
   * @throws IOException if an I/O error occurs
   */
  public void removeImage(String id) throws IOException {
    File file = getImageFile(id);
    Files.delete(file.toPath());
  }

  /**
   * Returns {@link BufferedImage} object representing the image deserialized from bytes array
   *
   * @param data array representing the image
   * @return buffered image
   * @throws IOException if an error occurs during reading
   */
  public BufferedImage decryptImage(byte[] data) throws IOException {
    InputStream s = new ByteArrayInputStream(data);
    BufferedImage image = ImageIO.read(s);
    s.close();
    return image;
  }

  /**
   * Serializes {@link BufferedImage} image into bytes array
   *
   * @param image image to serialize
   * @return byte array representing the image
   * @throws IOException if an error occurs during writing
   */
  public byte[] encryptImage(BufferedImage image) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(image, "bmp", baos);
    byte[] encrypted = baos.toByteArray();
    baos.close();
    return encrypted;
  }

  public String getPathForSaving() {
    return pathForSaving;
  }
}
