package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TestEntityManager em;

    private User author;
    private Item item1;
    private Item item2;
    private Comment comment1;
    private Comment comment2;

    @BeforeEach
    void setUp() {
        // Создаем и сохраняем автора комментария (пользователя)
        author = new User();
        author.setName("Комментатор");
        author.setEmail("commentator@mail.com");
        em.persist(author);

        // Создаем владельца для вещей
        User owner = new User();
        owner.setName("Владелец");
        owner.setEmail("owner@mail.com");
        em.persist(owner);

        // Создаем и сохраняем две разные вещи
        item1 = new Item();
        item1.setName("Дрель");
        item1.setDescription("Ударная");
        item1.setAvailable(true);
        item1.setOwner(owner);
        em.persist(item1);

        item2 = new Item();
        item2.setName("Палатка");
        item2.setDescription("Двухместная");
        item2.setAvailable(true);
        item2.setOwner(owner);
        em.persist(item2);

        // Создаем и сохраняем комментарий к первой вещи
        comment1 = new Comment();
        comment1.setText("Отличная дрель, все понравилось!");
        comment1.setItem(item1);
        comment1.setAuthor(author);
        comment1.setCreated(LocalDateTime.now());
        em.persist(comment1);

        // Создаем и сохраняем комментарий ко второй вещи
        comment2 = new Comment();
        comment2.setText("Палатка промокает под сильным дождем");
        comment2.setItem(item2);
        comment2.setAuthor(author);
        comment2.setCreated(LocalDateTime.now().minusDays(1));
        em.persist(comment2);

        em.flush();
    }

    @Test
    void findByItemId_shouldReturnComments_onlyForSpecificItem() {
        List<Comment> result = commentRepository.findByItemId(item1.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getText()).isEqualTo("Отличная дрель, все понравилось!");
        assertThat(result.get(0).getItem().getId()).isEqualTo(item1.getId());
    }

    @Test
    void findByItemId_shouldReturnEmptyList_whenItemHasNoComments() {
        List<Comment> result = commentRepository.findByItemId(999L); // Несуществующий ID

        assertThat(result).isEmpty();
    }

    @Test
    void findByItemIdIn_shouldReturnCommentsForMultipleItemIds_andFetchAuthor() {
        List<Long> itemIds = List.of(item1.getId(), item2.getId(), 999L);

        List<Comment> result = commentRepository.findByItemIdIn(itemIds);

        assertThat(result).hasSize(2);

        assertThat(result)
                .extracting(comment -> comment.getAuthor().getName())
                .containsOnly("Комментатор");

        assertThat(result)
                .extracting(Comment::getText)
                .containsExactlyInAnyOrder("Отличная дрель, все понравилось!", "Палатка промокает под сильным дождем");
    }

    @Test
    void findByItemIdIn_shouldReturnEmptyList_whenNoneOfItemsHaveComments() {
        List<Comment> result = commentRepository.findByItemIdIn(List.of(999L, 888L));

        assertThat(result).isEmpty();
    }
}
