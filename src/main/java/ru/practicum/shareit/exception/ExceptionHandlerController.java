package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.item.exception.ItemAccessDeniedException;
import ru.practicum.shareit.user.exception.DuplicateEmailException;

@RestControllerAdvice
@Slf4j
public class ExceptionHandlerController {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleUserEmailValidationException(final DuplicateEmailException e) {
        log.debug("Error: 409 CONFLICT {}", e.getMessage(), e);
        return "Error: " + e.getMessage();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleItemOwnershipException(final ItemAccessDeniedException e) {
        log.debug("Error: 403 FORBIDDEN {}", e.getMessage(), e);
        return "Error: " + e.getMessage();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException(final NotFoundException e) {
        log.debug("Error: 404 NOT_FOUND {}", e.getMessage(), e);
        return "Error: " + e.getMessage();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationException(final MethodArgumentNotValidException e) {
        log.debug("Validation Error: 400 BAD_REQUEST {}", e.getMessage(), e);
        return "Validation Error: " + e.getMessage();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleAllUnhandledExceptions(final Throwable e) {
        log.debug("Error: 500 INTERNAL_SERVER_ERROR {}", e.getMessage(), e);
        return "Error: " + e.getMessage();
    }
}
