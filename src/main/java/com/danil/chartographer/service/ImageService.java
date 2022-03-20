package com.danil.chartographer.service;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * ImageService is an interface for working with {@link BufferedImage} images. It is used for their
 * creation, updating, reading and removing.
 */
public interface ImageService {

  /**
   * Creates an image with given width and height and returns an id of that image. If directory
   * where images should be stored doesn't exist, it creates it.
   *
   * @return id of created image
   * @throws IOException if an error occurs during writing
   */
  String create(int width, int height) throws IOException;

  /**
   * Get file with image by it's id
   *
   * @param id id of the image
   * @return file that contains image
   */
  BufferedImage getImage(String id) throws IOException;

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
  void copy(int x0, int y0, int x1, int y1, int width, int height, BufferedImage source,
            BufferedImage destination);

  /**
   * Saves image with given name
   *
   * @param image - image to save
   * @param name  - id of the image
   * @throws IOException if an error occurs during writing
   */
  void save(BufferedImage image, String name) throws IOException;

  /**
   * Removes an image from file by given id
   *
   * @param id id of the image to remove
   * @throws IOException if an I/O error occurs
   */
  void removeImage(String id) throws IOException;

  /**
   * Returns {@link BufferedImage} object representing the image deserialized from bytes array
   *
   * @param data array representing the image
   * @return buffered image
   * @throws IOException if an error occurs during reading
   */
  BufferedImage decryptImage(byte[] data) throws IOException;

  /**
   * Serializes {@link BufferedImage} image into bytes array
   *
   * @param image image to serialize
   * @return byte array representing the image
   * @throws IOException if an error occurs during writing
   */
  byte[] encryptImage(BufferedImage image) throws IOException;

  String getPathForSaving();

}
