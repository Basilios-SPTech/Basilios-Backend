package com.basilios.basilios.exception;

import java.math.BigDecimal;

public class InvalidMenuFilterException extends RuntimeException {
    private final String filterType;
    private final Object filterValue;

    public InvalidMenuFilterException(String message, String filterType, Object filterValue) {
        super(message);
        this.filterType = filterType;
        this.filterValue = filterValue;
    }

    public static InvalidMenuFilterException invalidPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return new InvalidMenuFilterException(
                "Faixa de preço inválida. Preço mínimo (" + minPrice + ") não pode ser maior que preço máximo (" + maxPrice + ")",
                "PRICE_RANGE",
                "min: " + minPrice + ", max: " + maxPrice
        );
    }

    public static InvalidMenuFilterException invalidSearchTerm(String searchTerm) {
        return new InvalidMenuFilterException(
                "Termo de busca muito curto. Mínimo 2 caracteres.",
                "SEARCH_TERM",
                searchTerm
        );
    }

    public static InvalidMenuFilterException invalidSortDirection(String direction) {
        return new InvalidMenuFilterException(
                "Direção de ordenação inválida: " + direction + ". Use 'asc' ou 'desc'.",
                "SORT_DIRECTION",
                direction
        );
    }

    public String getFilterType() {
        return filterType;
    }

    public Object getFilterValue() {
        return filterValue;
    }
}
