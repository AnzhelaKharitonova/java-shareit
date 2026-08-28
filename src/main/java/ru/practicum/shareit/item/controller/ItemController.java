package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.util.Constants;

import java.util.Collection;

@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemResponseDto createItem(
            @RequestHeader(Constants.USER_ID_HEADER) Long userId,
            @Valid @RequestBody ItemCreateDto itemCreateDto) {
        return itemService.createItem(itemCreateDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto updateItem(
            @RequestHeader(Constants.USER_ID_HEADER) Long userId,
            @PathVariable("itemId") long itemId,
            @Valid @RequestBody ItemUpdateDto itemUpdateDto) {
        return itemService.updateItem(itemId, userId, itemUpdateDto);
    }

    @GetMapping("/{itemId}")
    public ItemResponseDto findItemById(@PathVariable("itemId") long itemId) {
        return itemService.findItemById(itemId);
    }

    @GetMapping
    public Collection<ItemResponseDto> findItemsByOwner(@RequestHeader(Constants.USER_ID_HEADER) Long userId) {
        return itemService.findItemsByOwner(userId);
    }

    @GetMapping("/search")
    public Collection<ItemResponseDto> search(
            @RequestParam String text) {
        return itemService.search(text);
    }

}


