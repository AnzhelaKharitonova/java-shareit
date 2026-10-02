package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(ItemClient.class)
class ItemClientTest {

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper objectMapper;

    private ItemCreateDto createDto;
    private ItemUpdateDto updateDto;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        createDto = ItemCreateDto.builder().name("Дрель").description("Bosch").available(true).build();
        updateDto = ItemUpdateDto.builder().name("Новая дрель").build();
        commentDto = CommentDto.builder().text("Отличный инструмент").build();
    }

    @Test
    void createItem_shouldSendPostRequest_andReturn201() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(createDto);

        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/items")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andExpect(content().json(jsonBody))
                .andRespond(withStatus(HttpStatus.CREATED).body("{\"id\":1}").contentType(MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = itemClient.createItem(createDto, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        mockServer.verify();
    }

    @Test
    void updateItem_shouldSendPatchRequest_withItemInPath() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(updateDto);

        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/items/1")))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("X-Sharer-User-Id", "2"))
                .andExpect(content().json(jsonBody))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = itemClient.updateItem(1L, 2L, updateDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }

    @Test
    void findItemById_shouldSendGetRequest_withoutUserHeader() {
        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/items/5")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(headerDoesNotExist("X-Sharer-User-Id"))
                .andRespond(withSuccess("{\"id\":5}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = itemClient.findItemById(5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }

    @Test
    void findItemsByOwner_shouldSendGetRequest_withUserHeader() {
        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/items")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "10"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = itemClient.findItemsByOwner(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }

    @Test
    void search_shouldSendGetRequest_withQueryParameter() {
        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString(
                "/items/search?text=%D0%B4%D1%80%D0%B5%D0%BB%D1%8C")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = itemClient.search("дрель");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }

    @Test
    void addComment_shouldSendPostRequest_withItemIdInPath_andUserHeader() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(commentDto);

        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/items/1/comment")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", "3"))
                .andExpect(content().json(jsonBody))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = itemClient.addComment(3L, 1L, commentDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }
}
