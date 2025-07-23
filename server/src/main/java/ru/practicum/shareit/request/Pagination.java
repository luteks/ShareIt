package ru.practicum.shareit.request;

import jakarta.validation.ValidationException;
import org.springframework.data.domain.PageRequest;

import static org.springframework.data.domain.PageRequest.of;

public class Pagination {
    public static PageRequest makePageRequest(Integer from, Integer size) {
        if (size == null || from == null) return null;
        if (size <= 0 || from < 0) throw new ValidationException("size <= 0 || from < 0");
        return of(from / size, size);
    }
}