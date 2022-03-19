package com.danil.chartographer.controller;

import com.danil.chartographer.Utilities;
import com.danil.chartographer.exception.NoSuchIdException;
import com.danil.chartographer.service.ImageService;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
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
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(args = "src/test/resources/images")
@AutoConfigureMockMvc()
class ImageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ImageService imageService;

  private BufferedImage charta;
  private BufferedImage fragment;
  private final String id = "12345";

  @BeforeEach
  void initImages() {
    charta = Utilities.generateImage(1000, 1000, Color.GREEN);
    fragment = Utilities.generateImage(300, 300, Color.BLACK);
  }

  @Test
  void createChart() throws Exception {
    String uuid = "12345";
    when(imageService.create(1000, 2000)).thenReturn(uuid);

    this.mockMvc.perform(post("/chartas")
            .param("width", "1000")
            .param("height", "2000"))
            .andExpect(status().isCreated())
            .andExpect(content().string(uuid));

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
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(fragment, "bmp", baos);
    byte[] data = baos.toByteArray();

    when(imageService.getImage(any(String.class))).thenReturn(charta);
    when(imageService.decryptImage(data)).thenReturn(fragment);

    mockMvc.perform(post("/chartas/" + id)
            .content(data)
            .param("x", "100")
            .param("y", "200")
            .param("width", String.valueOf(fragment.getWidth()))
            .param("height", String.valueOf(fragment.getHeight())))
            .andExpect(status().isOk());

    verify(imageService, times(1)).getImage(id);
    verify(imageService, times(1)).decryptImage(data);
    verify(imageService, times(1)).copy(0, 0, 100, 200,
            fragment.getWidth(), fragment.getHeight(), fragment, charta);
    verify(imageService, times(1)).save(charta, id);
  }

  @Test
  void saveFragmentWithXYCordsGreaterThanChartaSize() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(fragment, "bmp", baos);
    byte[] data = baos.toByteArray();

    when(imageService.getImage(any(String.class))).thenReturn(charta);
    when(imageService.decryptImage(data)).thenReturn(fragment);

    mockMvc.perform(post("/chartas/" + id)
            .content(data)
            .param("x", "500")
            .param("y", "2500")
            .param("width", String.valueOf(fragment.getWidth()))
            .param("height", String.valueOf(fragment.getHeight())))
            .andExpect(status().isBadRequest());

    verify(imageService, times(1)).getImage(id);
    verify(imageService, times(0)).decryptImage(any(byte[].class));
    verify(imageService, times(0)).copy(anyInt(), anyInt(), anyInt(), anyInt(),
            anyInt(), anyInt(), any(BufferedImage.class), any(BufferedImage.class));
    verify(imageService, times(0)).save(any(BufferedImage.class), anyString());
  }

  @Test
  void saveFragmentWithWrongWidthAndHeight() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(fragment, "bmp", baos);
    byte[] data = baos.toByteArray();

    when(imageService.getImage(any(String.class))).thenReturn(charta);
    when(imageService.decryptImage(data)).thenReturn(fragment);

    mockMvc.perform(post("/chartas/" + id)
            .content(data)
            .param("x", "500")
            .param("y", "300")
            .param("width", "500")
            .param("height", "205"))
            .andExpect(status().isBadRequest());

    verify(imageService, times(1)).getImage(id);
    verify(imageService, times(1)).decryptImage(data);
    verify(imageService, times(0)).copy(anyInt(), anyInt(), anyInt(), anyInt(),
            anyInt(), anyInt(), any(BufferedImage.class), any(BufferedImage.class));
    verify(imageService, times(0)).save(any(BufferedImage.class), anyString());
  }

  @Test
  void saveFragmentWithNonExistingId() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(fragment, "bmp", baos);
    byte[] data = baos.toByteArray();

    when(imageService.getImage(any(String.class))).thenThrow(NoSuchIdException.class);
    when(imageService.decryptImage(data)).thenReturn(fragment);

    mockMvc.perform(post("/chartas/" + id)
            .content(data)
            .param("x", "100")
            .param("y", "200")
            .param("width", String.valueOf(fragment.getWidth()))
            .param("height", String.valueOf(fragment.getHeight())))
            .andExpect(status().isNotFound());

    verify(imageService, times(1)).getImage(id);
    verify(imageService, times(0)).decryptImage(any(byte[].class));
    verify(imageService, times(0)).copy(anyInt(), anyInt(), anyInt(), anyInt(),
            anyInt(), anyInt(), any(BufferedImage.class), any(BufferedImage.class));
    verify(imageService, times(0)).save(any(BufferedImage.class), anyString());
  }

  @Test
  void getFragment() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(fragment, "bmp", baos);
    byte[] data = baos.toByteArray();

    when(imageService.getImage(id)).thenReturn(charta);
    when(imageService.encryptImage(any(BufferedImage.class))).thenReturn(data);

    mockMvc.perform(get("/chartas/" + id)
            .param("x", "100")
            .param("y", "200")
            .param("width", String.valueOf(fragment.getWidth()))
            .param("height", String.valueOf(fragment.getHeight())))
            .andExpect(status().isOk())
            .andExpect(content().bytes(data));

    verify(imageService, times(1)).getImage(id);
    verify(imageService, times(1)).copy(eq(100), eq(200), eq(0), eq(0),
            eq(300), eq(300), eq(charta), any(BufferedImage.class));
    verify(imageService, times(1)).encryptImage(any(BufferedImage.class));
  }

  @Test
  void getFragmentWithNegativeWidthAndHeight() throws Exception {
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
    verify(imageService, times(0)).copy(anyInt(), anyInt(), anyInt(), anyInt(),
            anyInt(), anyInt(), any(BufferedImage.class), any(BufferedImage.class));
    verify(imageService, times(0)).encryptImage(any(BufferedImage.class));
  }

  @Test
  void getFragmentWithNonExistingId() throws Exception {
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
    verify(imageService, times(0)).copy(anyInt(), anyInt(), anyInt(), anyInt(),
            anyInt(), anyInt(), any(BufferedImage.class), any(BufferedImage.class));
    verify(imageService, times(0)).encryptImage(any(BufferedImage.class));
  }

  @Test
  void removeChart() throws Exception {
    mockMvc.perform(delete("/chartas/" + id))
            .andExpect(status().isOk());

    verify(imageService, times(1)).removeImage(id);
  }

  @Test
  void removeChartWithNonExistingId() throws Exception {
    doThrow(NoSuchIdException.class).when(imageService).removeImage(id);

    mockMvc.perform(delete("/chartas/" + id))
            .andExpect(status().isNotFound());

    verify(imageService, times(1)).removeImage(id);
  }
}