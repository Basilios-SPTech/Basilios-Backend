package com.basilios.basilios.core.enums;

import lombok.Getter;

/**
 * Categorias principais de produtos do cardápio
 */
@Getter
public enum ProductCategory {
    BURGER("Hambúrguer", "Burgers artesanais e tradicionais"),
    SIDE("Acompanhamento", "Batatas, onion rings, saladas"),
    DRINK("Bebida", "Refrigerantes, sucos, milkshakes, cervejas"),
    DESSERT("Sobremesa", "Açai"),
    COMBO("Combo", "Combinações especiais com desconto");

    private final String displayName;
    private final String description;

    ProductCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Obtém a categoria a partir de uma string (case-insensitive)
     */
    public static ProductCategory fromString(String value) {
        if (value == null) {
            return null;
        }

        for (ProductCategory category : ProductCategory.values()) {
            if (category.name().equalsIgnoreCase(value) ||
                    category.displayName.equalsIgnoreCase(value)) {
                return category;
            }
        }

        throw new IllegalArgumentException("Categoria inválida: " + value);
    }

    /**
     * Verifica se a string é uma categoria válida
     */
    public static boolean isValid(String value) {
        try {
            fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}