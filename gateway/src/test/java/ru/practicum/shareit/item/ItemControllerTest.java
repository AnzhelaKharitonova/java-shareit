package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerGatewayTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void createItem_WhenValid_ReturnsOk() throws Exception {
        ItemCreateDto validDto = ItemCreateDto.builder()
                .name("Дрель")
                .description("Ударная дрель Bosch")
                .available(true)
                .build();

        when(itemClient.createItem(any(ItemCreateDto.class), anyLong()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk());
    }

    @Test
    void createItem_WhenNameIsBlank_ReturnsBadRequest() throws Exception {
        ItemCreateDto invalidDto = ItemCreateDto.builder()
                .name("   ")
                .description("Описание")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void createItem_WhenAvailableIsNull_ReturnsBadRequest() throws Exception {
        ItemCreateDto invalidDto = ItemCreateDto.builder()
                .name("Дрель")
                .description("Описание")
                .available(null)
                .build();

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void search_WhenTextIsBlank_ReturnsEmptyListDirectly() throws Exception {
        // Если строка пустая, контроллер должен сам вернуть пустой список без обращения к клиенту
        mockMvc.perform(get("/items/search")
                        .param("text", "    "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verifyNoInteractions(itemClient);
    }

    @Test
    void search_WhenTextIsValid_ReturnsOk() throws Exception {
        when(itemClient.search(anyString()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_WhenTextIsBlank_ReturnsBadRequest() throws Exception {
        // Нарушение @NotBlank для поля text в CommentDto
        CommentDto invalidComment = CommentDto.builder()
                .text("")
                .build();

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidComment)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void addComment_WhenValid_ReturnsOk() throws Exception {
        CommentDto validComment = CommentDto.builder()
                .text("Всё супер!")
                .build();

        when(itemClient.addComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validComment)))
                .andExpect(status().isOk());
    }
}
