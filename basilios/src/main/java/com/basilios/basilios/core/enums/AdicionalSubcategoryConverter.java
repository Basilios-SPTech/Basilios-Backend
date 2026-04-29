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
            return AdicionalSubcategory.OUTRO;
        }

        try {
            return AdicionalSubcategory.valueOf(dbData.trim());
        } catch (IllegalArgumentException ex) {
            // Fallback for legacy/invalid values
            return AdicionalSubcategory.OUTRO;
        }
    }
}

