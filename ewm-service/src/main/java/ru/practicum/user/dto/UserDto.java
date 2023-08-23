package ru.practicum.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter
@Valid
public class UserDto {
    private Long id;
    @NotBlank
    @Length(min = 2, max = 250)
    private String name;
    @Email
    @NotBlank
    @Length(min = 6, max = 254)
    private String email;
}
