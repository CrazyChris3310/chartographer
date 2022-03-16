package com.danil.chartographer.service;

import com.danil.chartographer.exception.NoSuchIdException;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.UUID;

@Service
public class ImageService {

    private final String pathForSaving;

    public ImageService(ApplicationArguments args) {
        List<String> files = args.getNonOptionArgs();
        pathForSaving = files.get(0);
    }

    public String create(int width, int height) throws IOException {
        File uploadDir = new File(pathForSaving);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        String uuid = UUID.randomUUID().toString();

        ImageIO.write(image, "bmp", new File(pathForSaving + "/" + uuid + ".bmp"));

        return uuid;
    }

    /**
     * Copies {@code width * height} pixels from source {@link BufferedImage} starting at point (x0, y0) to destination
     * {@link BufferedImage} starting at point (x1, y1)
     * @param x0 horizontal coordinate from where to start copying
     * @param y0 vertical coordinate from where to start copying
     * @param x1 horizontal coordinate to where to start copying
     * @param y1 vertical coordinate to where to start copying
     * @param source image from where content is copied
     * @param destination image to which content is being copied
     */
    public void copy(int x0, int y0, int x1, int y1, int width, int height, BufferedImage source, BufferedImage destination) {
        int realWidth = Math.min(destination.getWidth() - x1, width);
        int realHeight = Math.min(destination.getHeight() - y1, height);

        for (int i = 0; i < realWidth; ++i) {
            for (int j = 0; j < realHeight; ++j) {
                destination.setRGB(x1 + i, y1 + j, source.getRGB(x0 + i, y0 + j));
            }
        }
    }

    public void save(BufferedImage image, File file) throws IOException {
        ImageIO.write(image, "bmp", file);
    }

    public BufferedImage readImage(File file) throws IOException {
        return ImageIO.read(file);
    }

    public File getImageFile(String id) {
        File file = new File(pathForSaving + "/" + id + ".bmp");
        if (!file.exists()) {
            throw new NoSuchIdException("No charta with id " + id + " can be found");
        }

        return file;
    }

    public BufferedImage decryptImage(byte[] data) throws IOException {
        InputStream s = new ByteArrayInputStream(data);
        BufferedImage image =  ImageIO.read(s);
        s.close();
        return image;
    }

    public byte[] encryptImage(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "bmp", baos);
        byte[] encrypted = baos.toByteArray();
        baos.close();
        return encrypted;
    }

}
