package com.danil.chartographer;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Utilities {

  public static BufferedImage generateImage(int width, int height, Color color) {
    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
    for (int i = 0; i < img.getWidth(); ++i) {
      for (int j = 0; j < img.getHeight(); ++j) {
        img.setRGB(i, j, color.getRGB());
      }
    }
    return img;
  }
}
