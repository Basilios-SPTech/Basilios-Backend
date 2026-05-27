package com.basilios.basilios.app.dto.store;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class StoreHoursUpdateDTO {

    @NotEmpty(message = "hours deve conter pelo menos um dia")
    private List<@Valid StoreHoursDayDTO> hours;
}

