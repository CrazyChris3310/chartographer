package com.danil.chartographer.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

/**
 * Implementation of {@link ImageService} for working with BMP images
 */
@Service
public class BmpImageService extends AbstractImageService {
  /**
   * Takes command line arguments and finds there a directory for saving images
   *
   * @param args command line arguments
   */
  public BmpImageService(ApplicationArguments args) {
    super(args, "bmp", BufferedImage.TYPE_3BYTE_BGR);
  }
}
