package com.danil.chartographer.service;

import com.danil.chartographer.exception.NoSuchIdException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(args = "src/main/resources/test/images")
class ImageServiceTest {

    @Autowired
    private ImageService service;

    private BufferedImage generateImage(int width, int height, Color color) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        for (int i = 0; i < img.getWidth(); ++i) {
            for (int j = 0; j < img.getHeight(); ++j) {
                img.setRGB(i, j, color.getRGB());
            }
        }
        return img;
    }

    private void clearDirectory(File dir) throws IOException {
        for (String s : dir.list()) {
            Files.delete(Path.of(dir.getAbsolutePath(),"/",  s));
        }
    }

    @Test
    void createDirectory() throws IOException {
        File uploadPath = new File(service.getPathForSaving());
        if (uploadPath.exists()) {
            clearDirectory(uploadPath);
            Files.delete(uploadPath.toPath());
        }
        assertFalse(uploadPath.exists());

        service.create(100, 200);
        assertTrue(new File(service.getPathForSaving()).exists());
    }

    @Test
    void createFile() throws IOException {
        String name = service.create(5000, 2000);

        File file = new File(service.getPathForSaving() + "/" + name + ".bmp");
        assertTrue(file.exists());
    }

    @Test
    void copySmallToLarge() {
        BufferedImage src = generateImage(200, 300, Color.GREEN);
        BufferedImage dest = generateImage(1000, 1000, Color.BLACK);

        int x0 = 0;
        int y0 = 0;
        int x1 = 20;
        int y1 = 20;
        int width = src.getWidth();
        int height = src.getHeight();

        service.copy(x0, y0, x1, y1, width, height, src, dest);

        for (int i = 0; i < dest.getWidth(); ++i) {
            for (int j = 0; j < dest.getHeight(); ++j) {
                Color color;
                if ( i >= x1 && i < x1 + width && j >= y1 && j < y1 + height) {
                    color = Color.GREEN;
                } else {
                    color = Color.BLACK;
                }
                assertEquals(dest.getRGB(i, j), color.getRGB());
            }
        }
    }

    @Test
    void copyLargeToSmall() {
        BufferedImage dest = generateImage(200, 300, Color.BLACK);
        BufferedImage src = generateImage(1000, 1000, Color.GREEN);

        int x0 = 300;
        int y0 = 200;
        int x1 = 0;
        int y1 = 0;
        int width = dest.getWidth();
        int height = dest.getHeight();

        service.copy(x0, y0, x1, y1, width, height, src, dest);

        for (int i = 0; i < dest.getWidth(); ++i) {
            for (int j = 0; j < dest.getHeight(); ++j) {
                assertEquals(dest.getRGB(i, j), src.getRGB(x0 + i, y0 + j));
            }
        }
    }

    @Test
    void copyGeneralCase() {
        BufferedImage src = generateImage(500, 500, Color.GREEN);
        BufferedImage dest = generateImage(1000, 300, Color.BLACK);

        int x0 = 300;
        int y0 = 200;
        int x1 = 0;
        int y1 = 100;
        int width = 400;
        int height = 300;

        //    (0, 0)         (500, 0)               (0, 0)                      (1000, 0)
        //       _______________                      ____________________________
        //      |               |                    |                            |
        //      |    (300, 200) |            ->      |_________  (200, 100)       |
        //      |           ____|____                |         |                  |
        //      |          | 200|    |               |_________|__________________| (1000, 300)
        //      |      300 |    |    |               |         |
        //      |__________|____|    |               |         |
        //                 |         |               |_________|
        //                 |_________|

        service.copy(x0, y0, x1, y1, width, height, src, dest);

        for (int i = 0; i < dest.getWidth(); ++i) {
            for (int j = 0; j < dest.getHeight(); ++j) {
                int rgb;
                if (i >= 0 && i < 200 && j >= 100 && j < 300) {
                    rgb = Color.GREEN.getRGB();
                } else {
                    rgb = Color.black.getRGB();
                }
                assertEquals(dest.getRGB(i, j), rgb);
            }
        }
    }

    @Test
    void save() throws IOException {
        BufferedImage src = generateImage(200, 300, Color.GREEN);

        String name = "source_image";
        service.save(src, name);

        File file = new File(service.getPathForSaving() + "/" + name + ".bmp");
        assertTrue(file.exists());
    }

    @Test
    void getImage() throws IOException {
        BufferedImage src = generateImage(300, 400, Color.GREEN);

        String name = "source_image";
        service.save(src, name);

        BufferedImage res = service.getImage(name);

        assertEquals(src.getWidth(), res.getWidth());
        assertEquals(src.getHeight(), res.getHeight());
        assertEquals(src.getType(), res.getType());

        for (int i = 0; i < src.getWidth(); ++i) {
            for (int j = 0; j < src.getHeight(); ++j) {
                assertEquals(src.getRGB(i, j), res.getRGB(i, j));
            }
        }

    }

    @Test
    void getImageIfNoFile() throws IOException {
        File uploadPath = new File(service.getPathForSaving());
        clearDirectory(uploadPath);

        String name = "source_image";

        assertThrows(NoSuchIdException.class, () -> service.getImage(name));
    }

    @Test
    void removeImage() throws IOException {
        BufferedImage img = generateImage(800, 600, Color.BLUE);

        String name = "file_to_remove";
        service.save(img, name);
        service.removeImage(name);

        File file = new File(service.getPathForSaving() + "/" + name + ".bmp");
        assertFalse(file.exists());
    }

    @Test
    void removeImageIfNoFile() {
        BufferedImage img = generateImage(800, 600, Color.BLUE);

        String name = "no_file";

        assertThrows(NoSuchIdException.class, () -> service.removeImage(name));
    }


}