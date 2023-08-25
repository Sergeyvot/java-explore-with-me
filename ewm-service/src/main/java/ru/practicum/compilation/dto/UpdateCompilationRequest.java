package ru.practicum.compilation.dto;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import java.util.List;

@Builder(toBuilder = true)
@Getter
@Data
@Valid
public class UpdateCompilationRequest {
    private List<Integer> events;
    @Length(min = 1, max = 50)
    private String title;
    private Boolean pinned;
}
