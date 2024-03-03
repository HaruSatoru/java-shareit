package ru.practicum.shareit.item.exception;

import ru.practicum.shareit.exception.NotFoundException;

public class ItemNotFoundException extends NotFoundException {

    public ItemNotFoundException(Long itemId) {
        super("Item with identifier " + itemId +  " was not found.");
    }
}
