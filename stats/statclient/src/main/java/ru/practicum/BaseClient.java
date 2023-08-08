package ru.practicum;

import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.dto.ViewStatsDto;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BaseClient {
    protected final RestTemplate rest;

    public BaseClient(RestTemplate rest) {
        this.rest = rest;
    }

    protected List<ViewStatsDto> get(String path) {
        return get(path, null, null);
    }

    protected List<ViewStatsDto> get(String path, long userId) {
        return get(path, userId, null);
    }

    protected List<ViewStatsDto> get(String path, @Nullable Long userId, @Nullable Map<String, Object> parameters) {
        return (List<ViewStatsDto>) makeAndSendRequest(HttpMethod.GET, path, userId, parameters, null);
    }

    protected <T> void post(String path, T body) {
        post(path, null, null, body);
    }

    protected <T> void post(String path, long userId, T body) {
        post(path, userId, null, body);
    }

    protected <T> void post(String path, Long userId, @Nullable Map<String, Object> parameters, T body) {
        makeAndSendRequest(HttpMethod.POST, path, userId, parameters, body);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method, String path, Long userId, @Nullable Map<String, Object> parameters, @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders(userId));

        ResponseEntity<Object> ewnStatServerResponse;
        try {
            if (parameters != null) {
                ewnStatServerResponse = rest.exchange(path, method, requestEntity, Object.class, parameters);
            } else {
                ewnStatServerResponse = rest.exchange(path, method, requestEntity, Object.class);
            }
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
        return prepareGatewayResponse(ewnStatServerResponse);
    }

    private HttpHeaders defaultHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (userId != null) {
            headers.set("X-Sharer-User-Id", String.valueOf(userId));
        }
        return headers;
    }

    private ResponseEntity<Object> prepareGatewayResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }
}
