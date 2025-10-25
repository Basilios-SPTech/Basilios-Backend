// java
package com.basilios.basilios.app.mapper;

import com.basilios.basilios.app.dto.product.ProductResponseDTO;
import com.basilios.basilios.core.model.IngredientProduct;
import com.basilios.basilios.core.model.Product;
import com.basilios.basilios.core.model.Promotion;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    /**
     * Converte Product para ProductResponse (completo)
     */
    public ProductResponseDTO toResponse(Product product) {
        if (product == null) {
            return null;
        }

        ProductResponseDTO.ProductResponseDTOBuilder builder = ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .finalPrice(product.getFinalPrice())
                .isOnPromotion(product.isOnPromotion())
                .isPaused(product.getIsPaused())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt());

        // Adicionar ingredientes
        if (product.getProductIngredients() != null && !product.getProductIngredients().isEmpty()) {
            List<ProductResponseDTO.IngredientResponse> ingredients = product.getProductIngredients()
                    .stream()
                    .map(this::toIngredientResponse)
                    .collect(Collectors.toList());
            builder.ingredients(ingredients);
        }

        // Adicionar promoção se houver
        if (product.isOnPromotion()) {
            Promotion promotion = product.getBestCurrentPromotion();
            if (promotion != null) {
                builder.currentPromotion(toPromotionSummary(promotion, product.getPrice()));
            }
        }

        return builder.build();
    }

    /**
     * Converte lista de Products para lista de ProductResponse
     */
    public List<ProductResponseDTO> toResponseList(List<Product> products) {
        if (products == null) {
            return List.of();
        }
        return products.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Converte Product para ProductResponse simplificado (sem ingredientes)
     * Útil para listagens
     */
    public ProductResponseDTO toSimpleResponse(Product product) {
        if (product == null) {
            return null;
        }

        ProductResponseDTO.ProductResponseDTOBuilder builder = ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .finalPrice(product.getFinalPrice())
                .isOnPromotion(product.isOnPromotion())
                .isPaused(product.getIsPaused())
                .createdAt(product.getCreatedAt());

        // Adicionar promoção se houver
        if (product.isOnPromotion()) {
            Promotion promotion = product.getBestCurrentPromotion();
            if (promotion != null) {
                builder.currentPromotion(toPromotionSummary(promotion, product.getPrice()));
            }
        }

        return builder.build();
    }

    /**
     * Converte IngredientProduct para IngredientResponse
     */
    private ProductResponseDTO.IngredientResponse toIngredientResponse(IngredientProduct ip) {
        if (ip == null || ip.getIngredient() == null) {
            return null;
        }

        return ProductResponseDTO.IngredientResponse.builder()
                .id(ip.getIngredient().getId())
                .name(ip.getIngredient().getName())
                .quantity(ip.getQuantity())
                .measurementUnit(ip.getMeasurementUnit())
                .build();
    }

    /**
     * Converte Promotion para PromotionSummary
     */
    private ProductResponseDTO.PromotionSummary toPromotionSummary(Promotion promotion, BigDecimal originalPrice) {
        if (promotion == null) {
            return null;
        }

        BigDecimal savings = promotion.calculateDiscount(originalPrice);

        return ProductResponseDTO.PromotionSummary.builder()
                .id(promotion.getId())
                .title(promotion.getTitle())
                .discountPercentage(promotion.getDiscountPercentage())
                .discountAmount(promotion.getDiscountAmount())
                .savings(savings)
                .build();
    }
}
