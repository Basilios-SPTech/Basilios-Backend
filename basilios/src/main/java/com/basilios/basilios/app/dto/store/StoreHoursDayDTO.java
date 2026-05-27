package com.basilios.basilios.app.dto.store;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
public class StoreHoursDayDTO {

    @NotNull(message = "day_of_week e obrigatorio")
    @JsonProperty("day_of_week")
    private DayOfWeek dayOfWeek;

    @JsonProperty("is_closed")
    private boolean closed;

    @JsonProperty("opens_at")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime opensAt;

    @JsonProperty("closes_at")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closesAt;

    @AssertTrue(message = "Quando aberto, opens_at e closes_at sao obrigatorios e closes_at deve ser maior que opens_at")
    public boolean isTimeWindowValid() {
        if (closed) {
            return opensAt == null && closesAt == null;
        }
        return opensAt != null && closesAt != null && closesAt.isAfter(opensAt);
    }
}

