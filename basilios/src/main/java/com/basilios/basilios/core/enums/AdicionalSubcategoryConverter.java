package com.basilios.basilios.core.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AdicionalSubcategoryConverter implements AttributeConverter<AdicionalSubcategory, String> {

    @Override
    public String convertToDatabaseColumn(AdicionalSubcategory attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public AdicionalSubcategory convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return AdicionalSubcategory.ACOMPANHAMENTO;
        }

        String value = dbData.trim().toUpperCase();

        // Mapeia valores legados para manter compatibilidade com dados antigos.
        return switch (value) {
            case "BACON", "OVO" -> AdicionalSubcategory.PROTEINA;
            case "OUTRO" -> AdicionalSubcategory.ACOMPANHAMENTO;
            default -> {
                try {
                    yield AdicionalSubcategory.valueOf(value);
                } catch (IllegalArgumentException ex) {
                    yield AdicionalSubcategory.ACOMPANHAMENTO;
                }
            }
        };
    }
}
