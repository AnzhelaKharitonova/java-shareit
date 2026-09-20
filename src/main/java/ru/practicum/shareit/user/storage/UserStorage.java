package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

@Component
public interface UserStorage {

    Collection<User> findAllUsers();

    Optional<User> findUserById(Long id);

    User createUser(User user);

    User updateUser(Long userId, User user);

    void deleteUser(Long id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long userId);
}
