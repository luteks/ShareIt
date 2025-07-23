package ru.practicum.shareit.handler;

import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.response.ApiError;

@RestControllerAdvice(basePackages = {"ru.practicum.shareit.user.controller",
        "ru.practicum.shareit.item.controller",
        "ru.practicum.shareit.booking.controller",
        "ru.practicum.shareit.request.controller"})
public class ErrorHandler extends ResponseEntityExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ItemUnavailableException.class)
    public ApiError handleItemUnavailableException(final ItemUnavailableException e) {
        return new ApiError(HttpStatus.BAD_REQUEST, "Отказано в бронировании", e.getLocalizedMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CommentCreationException.class)
    public ApiError handleCommentCreationException(final CommentCreationException e) {
        return new ApiError(HttpStatus.BAD_REQUEST, "Ошибка при добавлении комментария",
                e.getLocalizedMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ValidationException.class)
    public ApiError handleValidationException(final ValidationException e) {
        return new ApiError(HttpStatus.BAD_REQUEST, "Ошибка валидации", e.getLocalizedMessage());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDuplicateEmailException(DuplicateEmailException e) {
        return new ApiError(HttpStatus.BAD_REQUEST,
                "Email уже есть в бд",
                e.getLocalizedMessage());
    }

    //404
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException.class)
    public ApiError handleEntityNotFoundException(final EntityNotFoundException e) {
        return new ApiError(HttpStatus.NOT_FOUND, "Обьект не найден", e.getLocalizedMessage());
    }

    //403
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ApiError handleAccessDeniedException(final AccessDeniedException e) {
        return new ApiError(HttpStatus.FORBIDDEN, "Отказано в доступе", e.getLocalizedMessage());
    }

    //500
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiError handleAll(final Exception ex, final WebRequest request) {
        return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage(), "Произошла ошибка");
    }
}