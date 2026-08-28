package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

@Component
public interface ItemStorage {

    Item createItem(Item newItem);

    Item updateItem(Long itemId, Item item);

    Optional<Item> findItemById(Long id);

    Collection<Item> findItemsByOwner(Long userId);

    Collection<Item> search(String text);

}
