package ru.practicum;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.dto.HitDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class HitDtoJsonTest {

    @Autowired
    private JacksonTester<HitDto> json;

    @Test
    void testHitDto() throws Exception {
        HitDto hitDto = new HitDto(
                1L,
                "ewm-main-service",
                "http://localhost:9090/hit",
                "172.16.13.98 /24", LocalDateTime.of(2023, 8, 25, 12, 0, 0));

        JsonContent<HitDto> result = json.write(hitDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.app").isEqualTo("ewm-main-service");
        assertThat(result).extractingJsonPathStringValue("$.uri").isEqualTo("http://localhost:9090/hit");
        assertThat(result).extractingJsonPathStringValue("$.ip").isEqualTo("172.16.13.98 /24");
        assertThat(result).extractingJsonPathStringValue("$.timestamp").isEqualTo("2023-08-25 12:00:00");
    }
}
