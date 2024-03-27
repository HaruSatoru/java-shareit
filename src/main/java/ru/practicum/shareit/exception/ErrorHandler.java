package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ValidationException;

import static ru.practicum.shareit.utils.Constants.UNKNOWN_ERROR_MESSAGE;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler({ValidationException.class, NotAvailableException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleRuntimeBadRequestExceptions(RuntimeException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation error occurred: {}", e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler({MissingRequestHeaderException.class, MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingRequestValueExceptions(MissingRequestValueException e) {
        log.warn("Error due to missing required request header: {}", e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler({NotFoundException.class, SecurityException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundRuntimeExceptions(RuntimeException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleAlreadyExistException(AlreadyExistException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(Throwable e) {
        log.error("An unknown error occurred: {}", e.getMessage(), e);
        return new ErrorResponse(UNKNOWN_ERROR_MESSAGE);
    }
}
