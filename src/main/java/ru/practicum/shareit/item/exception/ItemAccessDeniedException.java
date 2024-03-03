package ru.practicum.shareit.item.exception;

public class ItemAccessDeniedException extends RuntimeException {

    public ItemAccessDeniedException(String message) {
        super(message);
    }
}
