package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TestEntityManager em; // Специальный хелпер для подготовки тестовых данных в БД

    private User owner;
    private User requester;
    private Request request;
    private Item item;

    @BeforeEach
    void setUp() {
        // Создаем и сохраняем владельца вещи
        owner = new User();
        owner.setName("Владелец");
        owner.setEmail("owner@mail.com");
        em.persist(owner);

        // Создаем и сохраняем автора запроса на вещь
        requester = new User();
        requester.setName("Заказчик");
        requester.setEmail("request@mail.com");
        em.persist(requester);

        // Создаем и сохраняем запрос на вещь
        request = new Request();
        request.setDescription("Нужна дрель");
        request.setRequestor(requester);
        request.setCreated(LocalDateTime.now());
        em.persist(request);

        // Создаем и сохраняем саму вещь
        item = new Item();
        item.setName("Дрель ударная");
        item.setDescription("Мощная дрель для бетона");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);
        em.persist(item);

        em.flush(); // Синхронизируем состояние с БД
    }

    @Test
    void findByOwnerId_shouldReturnItems_whenOwnerHasItems() {
        List<Item> result = itemRepository.findByOwnerId(owner.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Дрель ударная");
        assertThat(result.get(0).getOwner().getId()).isEqualTo(owner.getId());
    }

    @Test
    void findByOwnerId_shouldReturnEmptyList_whenOwnerHasNoItems() {
        List<Item> result = itemRepository.findByOwnerId(999L); // Несуществующий ID

        assertThat(result).isEmpty();
    }

    @Test
    void findByRequestId_shouldReturnItems_whenRequestExists() {
        List<Item> result = itemRepository.findByRequestId(request.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRequest().getId()).isEqualTo(request.getId());
    }

    @Test
    void findByRequestIdIn_shouldReturnItemsForMultipleRequestIds() {
        List<Item> result = itemRepository.findByRequestIdIn(List.of(request.getId(), 999L));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Дрель ударная");
    }

    @Test
    void search_shouldFindItem_byNameCaseInsensitive() {
        List<Item> result = itemRepository.search("дРеЛь");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Дрель ударная");
    }

    @Test
    void search_shouldFindItem_byDescription() {
        List<Item> result = itemRepository.search("бетон");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).contains("бетона");
    }

    @Test
    void search_shouldNotReturnItem_whenItemIsNotAvailable() {
        item.setAvailable(false);
        em.persist(item);
        em.flush();

        List<Item> result = itemRepository.search("Дрель");

        assertThat(result).isEmpty();
    }

    @Test
    void search_shouldReturnEmptyList_whenTextDoesNotMatch() {
        List<Item> result = itemRepository.search("Отвертка");

        assertThat(result).isEmpty();
    }
}
