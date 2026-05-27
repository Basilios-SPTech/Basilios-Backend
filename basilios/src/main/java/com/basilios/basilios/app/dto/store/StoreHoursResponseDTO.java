package com.basilios.basilios.app.dto.store;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreHoursResponseDTO {
    private List<StoreHoursDayDTO> hours;
}

