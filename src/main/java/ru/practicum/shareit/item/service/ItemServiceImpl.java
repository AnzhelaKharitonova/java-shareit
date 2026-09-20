package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;
import java.util.Collections;

@Service
@AllArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private ItemMapper itemMapper;

    @Override
    public ItemResponseDto createItem(ItemCreateDto itemCreateDto, Long userId) {
        Item item = itemMapper.toItemFromCreateDto(itemCreateDto);
        item.setOwner(userStorage.findUserById(userId).orElseThrow(() -> {
            log.warn("Ошибка при добавлении вещи, пользователь с id {} не найден", userId);
            return new NotFoundException("Не найден пользователь с id " + userId);
        }));
        ItemResponseDto itemResponseDto = itemMapper.toItemResponseDto(itemStorage.createItem(item));
        log.info("Добавлена новая вещь с id = {}", itemResponseDto.getId());
        return itemResponseDto;
    }

    @Override
    public ItemResponseDto updateItem(Long itemId, Long userId, ItemUpdateDto itemUpdateDto) {

        findItemById(itemId);

        if (!itemStorage.findItemById(itemId).orElseThrow().getOwner().getId().equals(userId)) {
            log.warn("Ошибка при редактировании вещи, id владельца не совпадает");
            throw new NotFoundException("Редактировать вещь может только ее владелец!");
        }
        Item item = itemMapper.toItemFromUpdateDto(itemUpdateDto);
        Item updatedItem = itemStorage.updateItem(itemId, item);
        log.info("Обновлена информация о вещи с id {}", itemId);

        return itemMapper.toItemResponseDto(updatedItem);
    }

    @Override
    public ItemResponseDto findItemById(Long itemId) {
        return itemMapper.toItemResponseDto(itemStorage.findItemById(itemId).orElseThrow(() -> {
            log.warn("Вещь с id {} не найдена", itemId);
            return new NotFoundException("Не найдена вещь с id " + itemId);
        }));
    }

    @Override
    public Collection<ItemResponseDto> findItemsByOwner(Long userId) {
        userStorage.findUserById(userId).orElseThrow(() -> {
            log.warn("Ошибка при просмотре списка вещей, пользователь с id {} не найден", userId);
            return new NotFoundException("Не найден пользователь с id " + userId);
        });
        return itemMapper.toItemResponseDtoCollection(itemStorage.findItemsByOwner(userId));
    }

    @Override
    public Collection<ItemResponseDto> search(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        return itemMapper.toItemResponseDtoCollection(itemStorage.search(text));
    }

}
