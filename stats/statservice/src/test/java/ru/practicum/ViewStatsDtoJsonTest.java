package ru.practicum;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.dto.ViewStatsDto;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class ViewStatsDtoJsonTest {

    @Autowired
    private JacksonTester<ViewStatsDto> json;

    @Test
    void testViewStatsDto() throws Exception {
        ViewStatsDto viewStatsDto = new ViewStatsDto(
                "ewm-main-service",
                "http://localhost:9090/hit",
                3L);

        JsonContent<ViewStatsDto> result = json.write(viewStatsDto);

        assertThat(result).extractingJsonPathStringValue("$.app").isEqualTo("ewm-main-service");
        assertThat(result).extractingJsonPathStringValue("$.uri").isEqualTo("http://localhost:9090/hit");
        assertThat(result).extractingJsonPathNumberValue("$.hits").isEqualTo(3);
    }
}
