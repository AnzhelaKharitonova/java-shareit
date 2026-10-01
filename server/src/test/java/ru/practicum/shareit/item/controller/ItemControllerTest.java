package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void createItem_ShouldReturnItemAndStatus200() throws Exception {
        // Given
        ItemCreateDto createDto = ItemCreateDto.builder()
                .name("Дрель")
                .description("Дрель Bosch")
                .available(true)
                .build();

        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(1L)
                .name("Дрель")
                .build();

        when(itemService.createItem(any(ItemCreateDto.class), eq(1L)))
                .thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"));

        verify(itemService).createItem(any(ItemCreateDto.class), eq(1L));
    }

    @Test
    void updateItem_ShouldReturnUpdatedItem() throws Exception {
        // Given
        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("Дрель+")
                .build();

        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(1L)
                .name("Дрель+")
                .build();

        when(itemService.updateItem(eq(10L), eq(1L), any(ItemUpdateDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/items/10")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Дрель+"));

        verify(itemService).updateItem(eq(10L), eq(1L), any(ItemUpdateDto.class));
    }

    @Test
    void findItemById_ShouldReturnItem() throws Exception {
        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(5L)
                .name("Отвертка")
                .build();

        when(itemService.findItemById(5L)).thenReturn(responseDto);

        mockMvc.perform(get("/items/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Отвертка"));

        verify(itemService).findItemById(5L);
    }

    @Test
    void findItemsByOwner_ShouldReturnCollection() throws Exception {
        ItemResponseDto responseDto = ItemResponseDto.builder().id(1L).name("Пила").build();
        when(itemService.findItemsByOwner(1L)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Пила"));

        verify(itemService).findItemsByOwner(1L);
    }

    @Test
    void search_ShouldReturnCollection() throws Exception {
        ItemResponseDto responseDto = ItemResponseDto.builder().id(2L).name("Шуруповерт").build();
        when(itemService.search("дрель")).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("Шуруповерт"));

        verify(itemService).search("дрель");
    }

    @Test
    void addComment_ShouldReturnCommentDto() throws Exception {
        CommentDto commentDto = CommentDto.builder().text("Хороший инструмент").build();
        CommentDto responseComment = CommentDto.builder().id(10L).text("Хороший инструмент").build();

        when(itemService.addComment(eq(1L), eq(5L), any(CommentDto.class)))
                .thenReturn(responseComment);

        mockMvc.perform(post("/items/5/comment")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.text").value("Хороший инструмент"));

        verify(itemService).addComment(eq(1L), eq(5L), any(CommentDto.class));
    }
}
