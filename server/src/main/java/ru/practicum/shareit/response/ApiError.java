package ru.practicum.shareit.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ApiError {
    private HttpStatus status;
    private String message;
    private List<String> error;

    public ApiError(HttpStatus status, String message, String apiError) {
        this.status = status;
        this.message = message;
        error = Arrays.asList(apiError);
    }
}