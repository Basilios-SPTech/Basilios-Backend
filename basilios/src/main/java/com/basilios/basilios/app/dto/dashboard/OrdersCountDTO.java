package com.basilios.basilios.app.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdersCountDTO {
    private long orders;

    public static OrdersCountDTO toResponse(long value) {
        return OrdersCountDTO.builder().orders(value).build();
    }
}
