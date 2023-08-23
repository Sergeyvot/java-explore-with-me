package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Value;
import ru.practicum.event.model.Location;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter
@Valid
public class NewEventDto {
    @NotBlank(message = "Field: annotation. Error: must not be blank. Value: null")
    @Length(min = 20, max = 2000, message = "Field: annotation. Error: length of the field does not meet the restrictions")
    private String annotation;
    @NotNull(message = "Field: category. Error: must not be blank. Value: null")
    private Integer category;
    @NotBlank(message = "Field: description. Error: must not be blank. Value: null")
    @Length(min = 20, max = 7000, message = "Field: description. Error: length of the field does not meet the restrictions")
    private String description;
    @NotNull(message = "Field: eventDate. Error: must not be null. Value: null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    @NotNull(message = "Field: location. Error: must not be null. Value: null")
    private Location location;
    @Value("${some.key:false}")
    private Boolean paid;
    @Value("${some.key:0}")
    private Integer participantLimit;
    @Value("${some.key:true}")
    private Boolean requestModeration;
    @NotBlank(message = "Field: title. Error: must not be blank. Value: null")
    @Length(min = 3, max = 120, message = "Field: title. Error: length of the field does not meet the restrictions")
    private String title;
}
