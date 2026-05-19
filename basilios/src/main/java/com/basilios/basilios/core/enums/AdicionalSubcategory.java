package com.basilios.basilios.core.enums;

import lombok.Getter;

/**
 * Classifica o que o adicional É, independentemente de qual produto pertence.
 * A associação adicional ↔ produto é controlada pela tabela adicional_product.
 */
@Getter
public enum AdicionalSubcategory {

    PROTEINA("Proteína"),
    QUEIJO("Queijo"),
    MOLHO("Molho"),
    VEGETAL("Vegetal"),
    BEBIDA("Bebida"),
    OUTRO("Outro");

    private final String displayName;

    AdicionalSubcategory(String displayName) {
        this.displayName = displayName;
    }
}