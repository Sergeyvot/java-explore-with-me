package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorText handleNotFoundException(final NotFoundException e) {
        log.warn("Error 404. {}", e.getMessage());
        ApiErrorText apiErrorText = new ApiErrorText(e.getMessage(),
                "The required object was not found.",
                HttpStatus.NOT_FOUND);
        return apiErrorText;
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorText conflictException(final ConflictException e) {
        log.warn("Error 409. {}", e.getMessage());
        ApiErrorText apiErrorText = new ApiErrorText(e.getMessage(),
                "For the requested operation the conditions are not met.",
                HttpStatus.CONFLICT);
        return apiErrorText;
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorText handleValidationException(final ValidationException e) {
        log.warn("Error 400. {}", e.getMessage());
        ApiErrorText apiErrorText = new ApiErrorText(e.getMessage(),
                "Incorrectly made request.",
                HttpStatus.BAD_REQUEST);
        return apiErrorText;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorText handleThrowableException(final Throwable e) {
        log.warn("{}: {}",e.getClass().getSimpleName(), e.getMessage());
        ApiErrorText apiErrorText = new ApiErrorText(e.getMessage(),
                "Unexpected application error.",
                HttpStatus.INTERNAL_SERVER_ERROR);
        return apiErrorText;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorText handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        ApiErrorText apiErrorText = new ApiErrorText(e.getMessage(),
                "Incorrectly made request.",
                HttpStatus.BAD_REQUEST);
        log.warn("{}: {}",e.getClass().getSimpleName(), e.getMessage());
        return apiErrorText;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorText handleMissingServletRequestParameterException(final MissingServletRequestParameterException e) {
        ApiErrorText apiErrorText = new ApiErrorText(e.getMessage(),
                "Incorrectly made request.",
                HttpStatus.BAD_REQUEST);
        log.warn("{}: {}",e.getClass().getSimpleName(), e.getMessage());
        return apiErrorText;
    }
}
