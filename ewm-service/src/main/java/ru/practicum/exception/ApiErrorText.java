package ru.practicum.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@Setter
public class ApiErrorText {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String message;
    private String reason;
    private HttpStatus status;
    private String timestamp;

    public ApiErrorText(String message, String reason, HttpStatus status) {
        this.message = message;
        this.reason = reason;
        this.status = status;
        this.timestamp = LocalDateTime.now().format(FORMATTER);
    }
}
