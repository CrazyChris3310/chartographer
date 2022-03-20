package com.danil.chartographer.service;

import com.danil.chartographer.exception.NoSuchIdException;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.boot.ApplicationArguments;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

public abstract class AbstractImageService implements ImageService {

  private final String pathForSaving;
  private final String formatName;
  private final int imageType;

  /**
   * Takes command line arguments and finds there a directory for saving images
   *
   * @param args command line arguments
   */
  public AbstractImageService(ApplicationArguments args, String formatName, int imageType) {
    List<String> files = args.getNonOptionArgs();
    pathForSaving = files.get(0);
    this.imageType = imageType;
    this.formatName = formatName;
  }

  public String create(int width, int height) throws IOException {
    File uploadDir = new File(pathForSaving);
    if (!uploadDir.exists()) {
      boolean created = uploadDir.mkdirs();
      if (!created) {
        throw new IOException("Unable to create directories for saving images");
      }
    }

    BufferedImage image = new BufferedImage(width, height, this.imageType);
    String uuid = UUID.randomUUID().toString();

    ImageIO.write(image, formatName, new File(pathForSaving + "/" + uuid + "." +
                                              formatName));

    return uuid;
  }

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

  public void save(BufferedImage image, String name) throws IOException {
    ImageIO.write(image, formatName, new File(pathForSaving + "/" + name + "." +
                                              formatName));
  }

  public BufferedImage getImage(String id) throws IOException {
    File file = getImageFile(id);
    return ImageIO.read(file);
  }

  private File getImageFile(String id) {
    File file = new File(pathForSaving + "/" + id + "." + formatName);
    if (!file.exists()) {
      throw new NoSuchIdException("No charta with id " + id + " can be found");
    }

    return file;
  }

  public void removeImage(String id) throws IOException {
    File file = getImageFile(id);
    Files.delete(file.toPath());
  }

  public BufferedImage decryptImage(byte[] data) throws IOException {
    InputStream s = new ByteArrayInputStream(data);
    BufferedImage image = ImageIO.read(s);
    s.close();
    return image;
  }

  public byte[] encryptImage(BufferedImage image) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(image, formatName, baos);
    byte[] encrypted = baos.toByteArray();
    baos.close();
    return encrypted;
  }

  public String getPathForSaving() {
    return pathForSaving;
  }
}
