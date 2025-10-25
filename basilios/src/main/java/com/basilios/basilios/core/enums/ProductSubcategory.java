package com.basilios.basilios.core.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Subcategorias de produtos (ex.: tipos de hambúrguer, acompanhamentos, bebidas, sobremesas)
 */
@Getter
public enum ProductSubcategory {
    // BURGER subcategories
    BEEF("Carne Bovina", ProductCategory.BURGER),
    CHICKEN("Frango", ProductCategory.BURGER),
    PORK("Porco", ProductCategory.BURGER),
    FISH("Peixe", ProductCategory.BURGER),
    VEGETARIAN("Vegetariano", ProductCategory.BURGER),
    VEGAN("Vegano", ProductCategory.BURGER),

    // SIDE subcategories
    FRIES("Batata Frita", ProductCategory.SIDE),
    ONION_RINGS("Onion Rings", ProductCategory.SIDE),
    SALAD("Salada", ProductCategory.SIDE),
    NUGGETS("Nuggets", ProductCategory.SIDE),

    // DRINK subcategories
    SODA("Refrigerante", ProductCategory.DRINK),
    JUICE("Suco", ProductCategory.DRINK),
    MILKSHAKE("Milkshake", ProductCategory.DRINK),
    BEER("Cerveja", ProductCategory.DRINK),
    WATER("Água", ProductCategory.DRINK),

    // DESSERT subcategories
    ICE_CREAM("Sorvete", ProductCategory.DESSERT),
    CAKE("Bolo", ProductCategory.DESSERT),
    ACAI("Açaí", ProductCategory.DESSERT),
    PIE("Torta", ProductCategory.DESSERT);

    private final String displayName;
    private final ProductCategory category;

    ProductSubcategory(String displayName, ProductCategory category) {
        this.displayName = displayName;
        this.category = category;
    }

    /**
     * Obtém uma subcategoria a partir de uma string (case-insensitive)
     */
    public static ProductSubcategory fromString(String value) {
        if (value == null) return null;
        for (ProductSubcategory s : ProductSubcategory.values()) {
            if (s.name().equalsIgnoreCase(value) || s.displayName.equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Subcategoria inválida: " + value);
    }

    public static boolean isValid(String value) {
        try {
            fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Retorna todas as subcategorias de uma dada categoria (preserva ordem definida no enum)
     */
    public static List<ProductSubcategory> valuesByCategory(ProductCategory category) {
        List<ProductSubcategory> list = new ArrayList<>();
        if (category == null) return list;
        for (ProductSubcategory s : ProductSubcategory.values()) {
            if (s.getCategory() == category) {
                list.add(s);
            }
        }
        return list;
    }
}
