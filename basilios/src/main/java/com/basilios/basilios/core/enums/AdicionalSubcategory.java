package com.basilios.basilios.core.enums;

import lombok.Getter;

/**
 * Classifica o que o adicional É, independentemente de qual produto pertence.
 * A associação adicional ↔ produto é controlada pela tabela adicional_product.
 */
@Getter
public enum AdicionalSubcategory {

    QUEIJO("Queijo"),
    PROTEINA("Proteína"),
    BACON("Bacon"),
    OVO("Ovo"),
    MOLHO("Molho"),
    VEGETAL("Vegetal"),
    ACOMPANHAMENTO("Acompanhamento"),
    OUTRO("Outro");

    private final String displayName;

    AdicionalSubcategory(String displayName) {
        this.displayName = displayName;
    }
}