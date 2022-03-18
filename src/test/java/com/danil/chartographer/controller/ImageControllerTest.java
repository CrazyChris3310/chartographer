package com.danil.chartographer.controller;

import com.danil.chartographer.ImageGenerator;
import com.danil.chartographer.exception.NoSuchIdException;
import com.danil.chartographer.service.ImageService;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(args = "src/test/resources/images")
@AutoConfigureMockMvc()
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageService imageService;

    @Test
    void createChart() throws Exception {
        this.mockMvc.perform(post("/chartas")
        .param("width", "1000")
        .param("height", "2000"))
                .andExpect(status().isCreated());

        verify(imageService, times(1)).create(1000, 2000);
    }

    @Test
    void createChartWithInvalidSize() throws Exception {
        this.mockMvc.perform(post("/chartas")
        .param("width", "25000")
        .param("height", "30000"))
                .andExpect(status().isBadRequest());

        verify(imageService, times(0)).create(25000, 30000);
    }

    @Test
    void saveFragment() throws Exception {
        BufferedImage fragment = ImageGenerator.generateImage(300, 200, Color.GREEN);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(fragment, "bmp", baos);
        byte[] data = baos.toByteArray();

        BufferedImage chartas = new BufferedImage(1000, 1000, BufferedImage.TYPE_3BYTE_BGR);

        when(imageService.getImage(any(String.class))).thenReturn(chartas);
        when(imageService.decryptImage(data)).thenReturn(fragment);

        mockMvc.perform(post("/chartas/12345")
                .content(data)
                .param("x", "100")
                .param("y", "200")
                .param("width", String.valueOf(fragment.getWidth()))
                .param("height", String.valueOf(fragment.getHeight())))
                .andExpect(status().isOk());

        verify(imageService, times(1)).getImage("12345");
        verify(imageService, times(1)).decryptImage(data);
        verify(imageService, times(1)).copy(0, 0, 100, 200,
                fragment.getWidth(), fragment.getHeight(), fragment, chartas);
        verify(imageService, times(1)).save(chartas, "12345");
    }

    @Test
    void saveFragmentWithWrongXYCords() throws Exception {
        BufferedImage fragment = ImageGenerator.generateImage(300, 200, Color.GREEN);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(fragment, "bmp", baos);
        byte[] data = baos.toByteArray();

        BufferedImage chartas = new BufferedImage(1000, 1000, BufferedImage.TYPE_3BYTE_BGR);

        when(imageService.getImage(any(String.class))).thenReturn(chartas);
        when(imageService.decryptImage(data)).thenReturn(fragment);

        mockMvc.perform(post("/chartas/12345")
                .content(data)
                .param("x", "500")
                .param("y", "2500")
                .param("width", String.valueOf(fragment.getWidth()))
                .param("height", String.valueOf(fragment.getHeight())))
                .andExpect(status().isBadRequest());

        verify(imageService, times(1)).getImage("12345");
        verify(imageService, times(0)).decryptImage(data);
        verify(imageService, times(0)).copy(0, 0, 100, 200,
                fragment.getWidth(), fragment.getHeight(), fragment, chartas);
        verify(imageService, times(0)).save(chartas, "12345");
    }

    @Test
    void saveFragmentWithWrongWidthAndHeight() throws Exception {
        BufferedImage fragment = ImageGenerator.generateImage(500, 200, Color.GREEN);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(fragment, "bmp", baos);
        byte[] data = baos.toByteArray();

        BufferedImage chartas = new BufferedImage(1000, 1000, BufferedImage.TYPE_3BYTE_BGR);

        when(imageService.getImage(any(String.class))).thenReturn(chartas);
        when(imageService.decryptImage(data)).thenReturn(fragment);

        mockMvc.perform(post("/chartas/12345")
                .content(data)
                .param("x", "500")
                .param("y", "300")
                .param("width", "500")
                .param("height", "205"))
                .andExpect(status().isBadRequest());

        verify(imageService, times(1)).getImage("12345");
        verify(imageService, times(1)).decryptImage(data);
        verify(imageService, times(0)).copy(0, 0, 100, 200,
                fragment.getWidth(), fragment.getHeight(), fragment, chartas);
        verify(imageService, times(0)).save(chartas, "12345");
    }

    @Test
    void saveFragmentWithNonExistingId() throws Exception {
        BufferedImage fragment = ImageGenerator.generateImage(300, 200, Color.GREEN);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(fragment, "bmp", baos);
        byte[] data = baos.toByteArray();

        BufferedImage chartas = new BufferedImage(1000, 1000, BufferedImage.TYPE_3BYTE_BGR);

        when(imageService.getImage(any(String.class))).thenThrow(NoSuchIdException.class);
        when(imageService.decryptImage(data)).thenReturn(fragment);

        mockMvc.perform(post("/chartas/12345")
                .content(data)
                .param("x", "100")
                .param("y", "200")
                .param("width", String.valueOf(fragment.getWidth()))
                .param("height", String.valueOf(fragment.getHeight())))
                .andExpect(status().isNotFound());

        verify(imageService, times(1)).getImage("12345");
        verify(imageService, times(0)).decryptImage(data);
        verify(imageService, times(0)).copy(0, 0, 100, 200,
                fragment.getWidth(), fragment.getHeight(), fragment, chartas);
        verify(imageService, times(0)).save(chartas, "12345");
    }

    @Test
    void getFragment() throws Exception {

        String id = "12345";
        BufferedImage charta = ImageGenerator.generateImage(1000, 1000, Color.GREEN);

        BufferedImage fragment = new BufferedImage(300, 300, charta.getType());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(fragment, "bmp", baos);
        byte[] data = baos.toByteArray();

        when(imageService.getImage(id)).thenReturn(charta);
        when(imageService.encryptImage(any(BufferedImage.class))).thenReturn(data);

        mockMvc.perform(get("/chartas/" + id)
                .param("x", "100")
                .param("y", "200")
                .param("width", "300")
                .param("height", "300"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(data));

        verify(imageService, times(1)).getImage(id);
        verify(imageService, times(1)).copy(eq(100), eq(200), eq(0), eq(0),
                eq(300), eq(300), eq(charta), any(BufferedImage.class));
        verify(imageService, times(1)).encryptImage(any(BufferedImage.class));
    }

    @Test
    void getFragmentWithNegativeWidthAndHeight() throws Exception {

        String id = "12345";
        BufferedImage charta = ImageGenerator.generateImage(1000, 1000, Color.GREEN);

        BufferedImage fragment = new BufferedImage(300, 300, charta.getType());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(fragment, "bmp", baos);
        byte[] data = baos.toByteArray();

        when(imageService.getImage(id)).thenReturn(charta);
        when(imageService.encryptImage(any(BufferedImage.class))).thenReturn(data);

        mockMvc.perform(get("/chartas/" + id)
                .param("x", "100")
                .param("y", "200")
                .param("width", "-300")
                .param("height", "-300"))
                .andExpect(status().isBadRequest());

        verify(imageService, times(0)).getImage(id);
        verify(imageService, times(0)).copy(eq(100), eq(200), eq(0), eq(0),
                eq(300), eq(300), eq(charta), any(BufferedImage.class));
        verify(imageService, times(0)).encryptImage(any(BufferedImage.class));
    }

    @Test
    void getFragmentWithNonExistingId() throws Exception {

        String id = "12345";
        BufferedImage charta = ImageGenerator.generateImage(1000, 1000, Color.GREEN);

        BufferedImage fragment = new BufferedImage(300, 300, charta.getType());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(fragment, "bmp", baos);
        byte[] data = baos.toByteArray();

        when(imageService.getImage(id)).thenThrow(NoSuchIdException.class);
        when(imageService.encryptImage(any(BufferedImage.class))).thenReturn(data);

        mockMvc.perform(get("/chartas/" + id)
                .param("x", "100")
                .param("y", "200")
                .param("width", "300")
                .param("height", "3000"))
                .andExpect(status().isNotFound());

        verify(imageService, times(1)).getImage(id);
        verify(imageService, times(0)).copy(eq(100), eq(200), eq(0), eq(0),
                eq(300), eq(300), eq(charta), any(BufferedImage.class));
        verify(imageService, times(0)).encryptImage(any(BufferedImage.class));
    }

    @Test
    void removeChart() throws Exception {
        mockMvc.perform(delete("/chartas/12345"))
                .andExpect(status().isOk());

        verify(imageService, times(1)).removeImage("12345");
    }
}