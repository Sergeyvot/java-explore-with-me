package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import ru.practicum.event.model.Location;

import javax.validation.Valid;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter
@Valid
public class UpdateEventDto {
    @Length(min = 20, max = 2000, message = "Field: annotation. Error: length of the field does not meet the restrictions")
    private String annotation;
    private Integer category;
    @Length(min = 20, max = 7000, message = "Field: description. Error: length of the field does not meet the restrictions")
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private String stateAction;
    @Length(min = 3, max = 120, message = "Field: title. Error: length of the field does not meet the restrictions")
    private String title;
}
