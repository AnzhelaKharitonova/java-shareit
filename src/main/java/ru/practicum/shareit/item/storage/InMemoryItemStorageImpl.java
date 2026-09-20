package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryItemStorageImpl implements ItemStorage {

    Map<Long, Item> items = new HashMap<>();

    @Override
    public Item createItem(Item newItem) {
        newItem.setId(generateId());
        items.put(newItem.getId(), newItem);
        return newItem;
    }

    @Override
    public Item updateItem(Long itemId, Item item) {

        Item updatableItem = items.get(itemId);

        if (item.getName() != null) {
            updatableItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            updatableItem.setDescription(item.getDescription());
        }
        if (updatableItem.getAvailable() != null) {
            updatableItem.setAvailable(item.getAvailable());
        }

        items.put(updatableItem.getId(), updatableItem);
        return updatableItem;
    }

    @Override
    public Optional<Item> findItemById(Long id) {
        Item item = items.get(id);
        if (item == null) {
            return Optional.empty();
        }
        return Optional.of(item);
    }

    @Override
    public Collection<Item> findItemsByOwner(Long userId) {
        return items.values().stream()
                .filter((item) -> item.getOwner().getId().equals(userId))
                .toList();
    }

    @Override
    public Collection<Item> search(String text) {
        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> (item.getName().toLowerCase().contains(text.toLowerCase()))
                        || (item.getDescription().toLowerCase().contains(text.toLowerCase())))
                .toList();
    }

    private Long generateId() {
        long maxId = items.keySet().stream().mapToLong(id -> id).max().orElse(0);
        return ++maxId;
    }

}

