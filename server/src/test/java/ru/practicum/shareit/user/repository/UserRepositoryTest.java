package ru.practicum.shareit.user.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager em;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setName("User One");
        user1.setEmail("one@mail.com");
        em.persist(user1);

        user2 = new User();
        user2.setName("User Two");
        user2.setEmail("two@mail.com");
        em.persist(user2);

        em.flush();
    }

    @Test
    void findAll_shouldReturnPageOfUsers() {
        Pageable pageable = PageRequest.of(0, 1);

        Page<User> resultPage = userRepository.findAll(pageable);

        assertThat(resultPage.getContent()).hasSize(1);
        assertThat(resultPage.getTotalElements()).isEqualTo(2); // Всего в базе 2 пользователя
        assertThat(resultPage.getContent().get(0).getEmail()).isEqualTo("one@mail.com");
    }

    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExists() {
        boolean exists = userRepository.existsByEmail("one@mail.com");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenEmailDoesNotExist() {
        boolean exists = userRepository.existsByEmail("unknown@mail.com");

        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmailAndIdNot_shouldReturnTrue_whenAnotherUserHasThisEmail() {
        boolean exists = userRepository.existsByEmailAndIdNot("one@mail.com", user2.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmailAndIdNot_shouldReturnFalse_whenSameUserHasThisEmail() {
        boolean exists = userRepository.existsByEmailAndIdNot("one@mail.com", user1.getId());

        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmailAndIdNot_shouldReturnFalse_whenEmailDoesNotExistAtAll() {
        boolean exists = userRepository.existsByEmailAndIdNot("free@mail.com", user1.getId());

        assertThat(exists).isFalse();
    }
}
