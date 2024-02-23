package ru.practicum.shareit.user.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException() {
        super("A user with this email already exists.");
    }

}
