package com.basilios.basilios.app.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemsSoldDTO {
    private long itemsSold;

    public static ItemsSoldDTO toResponse(long value) {
        return ItemsSoldDTO.builder().itemsSold(value).build();
    }
}
