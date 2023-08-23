package ru.practicum.request;

import lombok.experimental.UtilityClass;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.Request;

import java.time.LocalDateTime;
import java.time.ZoneId;

@UtilityClass
public class RequestMapperUtil {

    public ParticipationRequestDto toParticipationRequestDto(Request request) {
        ParticipationRequestDto.ParticipationRequestDtoBuilder requestDto = ParticipationRequestDto.builder();

        requestDto.id(request.getId());
        requestDto.created(LocalDateTime.ofInstant(request.getCreated(), ZoneId.systemDefault()));
        requestDto.requester(request.getRequester());
        requestDto.event(request.getEvent());
        requestDto.status(request.getStatus().name());

        return requestDto.build();
    }
}
