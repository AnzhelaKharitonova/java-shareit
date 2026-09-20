package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryUserStorageImpl implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAllUsers() {
        return users.values();
    }

    @Override
    public Optional<User> findUserById(Long id) {
        User user = users.get(id);
        if (user == null) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    @Override
    public User createUser(User newUser) {
        newUser.setId(generateId());
        users.put(newUser.getId(), newUser);
        return newUser;
    }

    @Override
    public User updateUser(Long userId, User user) {

        User updatableUser = users.get(userId);

        if (user.getName() != null) {
            updatableUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            updatableUser.setEmail(user.getEmail());
        }

        users.put(updatableUser.getId(), updatableUser);
        return updatableUser;
    }

    @Override
    public void deleteUser(Long id) {
        users.remove(id);
    }


    @Override
    public boolean existsByEmail(String email) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, Long userId) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email)
                        && !user.getId().equals(userId));
    }

    private Long generateId() {
        long maxId = users.keySet().stream().mapToLong(id -> id).max().orElse(0);
        return ++maxId;
    }

}

