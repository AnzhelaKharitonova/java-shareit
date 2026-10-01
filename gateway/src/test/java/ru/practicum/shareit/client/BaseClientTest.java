package ru.practicum.shareit.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class BaseClientTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;
    private TestClient testClient;

    static class TestClient extends BaseClient {
        public TestClient(RestTemplate rest) {
            super(rest);
        }
    }

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        testClient = new TestClient(restTemplate);
    }

    @Test
    void testGetMethods() {
        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/items")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess("{\"key\":\"val\"}", MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/items/1")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "42"))
                .andRespond(withSuccess());

        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/items/search")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess());

        ResponseEntity<Object> response1 = testClient.get("/items");
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Object> response2 = testClient.get("/items/1", 42L);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Object> response3 = testClient.get("/items/search", Map.of("text", "drill"));
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.OK);

        mockServer.verify();
    }

    @Test
    void testPostMethods() {
        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/items")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("test-body"))
                .andRespond(withStatus(HttpStatus.CREATED).body("{\"id\":1}").contentType(MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/items")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", "10"))
                .andExpect(content().string("body"))
                .andRespond(withSuccess());

        ResponseEntity<Object> response1 = testClient.post("/items", "test-body");
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Object> response2 = testClient.post("/items", 10L, "body");
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);

        mockServer.verify();
    }

    @Test
    void testPutMethods() {
        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/items/1")))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess());

        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/items/1")))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess());

        ResponseEntity<Object> response1 = testClient.put("/items/1", 1L, "update");
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Object> response2 = testClient.put("/items/1", 1L, Map.of("archive", true), "update");
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);

        mockServer.verify();
    }

    @Test
    void testPatchMethods() {
        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/users")))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withSuccess());

        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/users/2")))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("X-Sharer-User-Id", "2"))
                .andRespond(withSuccess());

        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/users/3")))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("X-Sharer-User-Id", "3"))
                .andRespond(withSuccess());

        ResponseEntity<Object> response1 = testClient.patch("/users", "body");
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Object> response2 = testClient.patch("/users/2", 2L);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Object> response3 = testClient.patch("/users/3", 3L, "body");
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.OK);

        mockServer.verify();
    }

    @Test
    void testDeleteMethods() {
        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/clear")))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/items/1")))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header("X-Sharer-User-Id", "5"))
                .andRespond(withSuccess());

        ResponseEntity<Object> response1 = testClient.delete("/clear");
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Object> response2 = testClient.delete("/items/1", 5L);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);

        mockServer.verify();
    }

    @Test
    void makeAndSendRequest_shouldReturnErrorResponse_whenHttpStatusCodeExceptionOccurs() {
        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/error-endpoint")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).body("Not Found Error Body"));

        ResponseEntity<Object> response = testClient.get("/error-endpoint");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isInstanceOf(byte[].class);
        assertThat(new String((byte[]) response.getBody())).isEqualTo("Not Found Error Body");

        mockServer.verify();
    }

    @Test
    void prepareGatewayResponse_shouldReturnResponseWithBody_whenStatusIsNot2xxAndHasBody() {
        mockServer.expect(requestTo(org.hamcrest.CoreMatchers.containsString("/bad-request")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.MULTIPLE_CHOICES).body("{\"error\":\"redirect\"}").contentType(MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = testClient.get("/bad-request");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.MULTIPLE_CHOICES);
        mockServer.verify();
    }
}
