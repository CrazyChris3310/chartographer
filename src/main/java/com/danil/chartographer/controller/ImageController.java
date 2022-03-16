package com.danil.chartographer.controller;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.websocket.server.PathParam;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chartas")
public class ImageController {

    private final String pathForSaving;

    public ImageController(ApplicationArguments args) {
        List<String> files = args.getNonOptionArgs();
        pathForSaving = files.get(0);
    }

    @PostMapping()
    public String createChart(@RequestParam int width, @RequestParam int height) throws IOException {
        File uploadDir = new File(pathForSaving);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        String uuid = UUID.randomUUID().toString();

        ImageIO.write(image, "bmp", new File(pathForSaving + "/" + uuid));

        return uuid;
    }

    @PostMapping("/{id}")
    public void saveFragment(@PathParam("id") String id,
                             @RequestParam int x,
                             @RequestParam int y,
                             @RequestParam int width,
                             @RequestParam int height) {

    }

    @GetMapping("/{id}")
    public void getFragment(@PathParam("id") String id,
                            @RequestParam int x,
                            @RequestParam int y,
                            @RequestParam int width,
                            @RequestParam int height) {

    }

    @DeleteMapping("/{id}")
    public void removeChart(@PathParam("id") String id) {

    }


}
