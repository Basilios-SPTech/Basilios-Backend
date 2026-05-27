package com.basilios.basilios.core.enums;

import lombok.Getter;

/**
 * Classifica o que o adicional e, independentemente de qual produto pertence.
 * A associacao adicional <-> produto e controlada pela tabela adicional_product.
 */
@Getter
public enum AdicionalSubcategory {

    PROTEINA("Proteína"),
    MOLHO("Molho"),
    VEGETAL("Vegetal"),
    BEBIDA("Bebida"),
    ACOMPANHAMENTO("Acompanhamento"),
    PAO("Pão");

    private final String displayName;

    AdicionalSubcategory(String displayName) {
        this.displayName = displayName;
    }
}