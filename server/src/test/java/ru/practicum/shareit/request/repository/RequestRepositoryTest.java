package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RequestRepositoryTest {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private TestEntityManager em;

    private User requestor1;
    private User requestor2;
    private Request request1;
    private Request request2;

    @BeforeEach
    void setUp() {
        requestor1 = new User();
        requestor1.setName("User One");
        requestor1.setEmail("one@mail.com");
        em.persist(requestor1);

        request1 = new Request();
        request1.setDescription("Нужна стремянка");
        request1.setRequestor(requestor1); // Проверь имя сеттера в своей модели (setRequestor или setRequestorId)
        request1.setCreated(LocalDateTime.now().minusDays(1));
        em.persist(request1);

        requestor2 = new User();
        requestor2.setName("User Two");
        requestor2.setEmail("two@mail.com");
        em.persist(requestor2);

        request2 = new Request();
        request2.setDescription("Ищу палатку");
        request2.setRequestor(requestor2);
        request2.setCreated(LocalDateTime.now());
        em.persist(request2);

        em.flush();
    }

    @Test
    void findByRequestorId_shouldReturnRequests_onlyForSpecificRequestor() {
        List<Request> result = requestRepository.findByRequestorId(requestor1.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo("Нужна стремянка");
        assertThat(result.get(0).getRequestor().getId()).isEqualTo(requestor1.getId());
    }

    @Test
    void findByRequestorId_shouldReturnEmptyList_whenRequestorHasNoRequests() {
        List<Request> result = requestRepository.findByRequestorId(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByRequestorIdNot_shouldReturnOtherUsersRequests_withPaginationAndSorting() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("created").descending());

        List<Request> result = requestRepository.findAllByRequestorIdNot(requestor1.getId(), pageable);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo("Ищу палатку");
        assertThat(result.get(0).getRequestor().getId()).isEqualTo(requestor2.getId());
    }

    @Test
    void findAllByRequestorIdNot_shouldReturnEmptyList_ifThereAreNoOtherRequests() {
        Pageable pageable = PageRequest.of(0, 10);

        List<Request> resultAll = requestRepository.findAllByRequestorIdNot(999L, pageable);
        assertThat(resultAll).hasSize(2);

        em.remove(request2);
        em.flush();

        List<Request> resultEmpty = requestRepository.findAllByRequestorIdNot(requestor1.getId(), pageable);
        assertThat(resultEmpty).isEmpty();
    }
}
