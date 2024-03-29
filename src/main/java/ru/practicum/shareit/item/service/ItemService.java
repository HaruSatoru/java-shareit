package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(long userId, ItemDto itemDto);

    ItemDto updateItem(long userId, ItemDto itemDto);

    ItemWithBookingDto retrieveItem(long itemId, long userId);

    List<ItemWithBookingDto> getUsersItems(long userId);

    List<ItemDto> findItemsByText(String text);

    CommentDto postUserComment(CommentDto commentDto, long userId, long itemId);
}