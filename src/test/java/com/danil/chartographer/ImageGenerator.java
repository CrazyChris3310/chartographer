package com.danil.chartographer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ImageGenerator {

    public static BufferedImage generateImage(int width, int height, Color color) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        for (int i = 0; i < img.getWidth(); ++i) {
            for (int j = 0; j < img.getHeight(); ++j) {
                img.setRGB(i, j, color.getRGB());
            }
        }
        return img;
    }

    public static void clearDirectory(File dir) throws IOException {
        for (String s : dir.list()) {
            Files.delete(Path.of(dir.getAbsolutePath(),"/",  s));
        }
    }
}
