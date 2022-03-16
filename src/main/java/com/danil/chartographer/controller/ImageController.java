package com.danil.chartographer.controller;

import com.danil.chartographer.domain.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.websocket.server.PathParam;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/chartas")
public class ImageController {

    private final String pathForSaving;

    public ImageController(ApplicationArguments args) {
        List<String> files = args.getNonOptionArgs();
        pathForSaving = files.get(0);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
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

    @GetMapping
    public List<String> getEverithing(@RequestParam boolean exception) throws IOException {
        if (exception)
            throw new IOException("Request required the exception");

        return Collections.singletonList("Fireball");
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response handleResponse(IOException e) {
        log.error(e.getMessage());
        return new Response(e.getMessage());
    }

}
