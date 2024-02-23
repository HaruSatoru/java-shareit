package ru.practicum.shareit.user.exception;

import ru.practicum.shareit.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(Long userId) {
        super("User with identifier " + userId + " not found.");
    }
}
