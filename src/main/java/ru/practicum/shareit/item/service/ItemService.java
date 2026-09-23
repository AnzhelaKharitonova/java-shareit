package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.Collection;

@Service
public interface ItemService {

    ItemResponseDto createItem(ItemCreateDto itemCreateDto, Long userId);

    ItemResponseDto updateItem(Long itemId, Long userId, ItemUpdateDto itemUpdateDto);

    ItemResponseDto findItemById(Long itemId);

    Collection<ItemResponseDto> findItemsByOwner(Long userId);

    Collection<ItemResponseDto> search(String text);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}
