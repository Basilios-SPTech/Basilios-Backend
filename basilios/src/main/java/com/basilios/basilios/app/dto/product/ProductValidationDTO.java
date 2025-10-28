package com.basilios.basilios.app.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductValidationDTO {
    private boolean isValid;
    private List<String> errors;
    private List<String> warnings;
    private Map<String, Object> validationDetails;

    public static ProductValidationDTO valid() {
        return ProductValidationDTO.builder()
                .isValid(true)
                .build();
    }

    public static ProductValidationDTO invalid(List<String> errors) {
        return ProductValidationDTO.builder()
                .isValid(false)
                .errors(errors)
                .build();
    }
}