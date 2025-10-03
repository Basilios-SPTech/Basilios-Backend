package com.basilios.basilios.app.dto.menu;

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
public class ProdutoValidationDTO {
    private boolean isValid;
    private List<String> errors;
    private List<String> warnings;
    private Map<String, Object> validationDetails;

    public static ProdutoValidationDTO valid() {
        return ProdutoValidationDTO.builder()
                .isValid(true)
                .build();
    }

    public static ProdutoValidationDTO invalid(List<String> errors) {
        return ProdutoValidationDTO.builder()
                .isValid(false)
                .errors(errors)
                .build();
    }
}