package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.promotion.CreatePromotionDTO;
import com.basilios.basilios.app.dto.promotion.PromotionCurrentDTO;
import com.basilios.basilios.app.dto.promotion.UpdatePromotionDTO;
import com.basilios.basilios.app.dto.promotion.PromotionResponseDTO;
import com.basilios.basilios.core.model.Product;
import com.basilios.basilios.core.model.Promotion;
import com.basilios.basilios.core.exception.NotFoundException;
import com.basilios.basilios.infra.repository.ProductRepository;
import com.basilios.basilios.infra.repository.PromotionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;

    public PromotionService(PromotionRepository promotionRepository, ProductRepository productRepository) {
        this.promotionRepository = promotionRepository;
        this.productRepository = productRepository;
    }

    public Promotion createPromotion(CreatePromotionDTO dto) {

        Promotion promotion = new Promotion();

        promotion.setTitle(dto.getTitle());
        promotion.setDescription(dto.getDescription());
        promotion.setDiscountPercentage(dto.getDiscountPercentage());
        promotion.setDiscountAmount(dto.getDiscountAmount());
        promotion.setStartDate(dto.getStartDate());
        promotion.setEndDate(dto.getEndDate());

        if (dto.getProductIds() != null && !dto.getProductIds().isEmpty()) {

            List<Product> products =
                    productRepository.findAllById(dto.getProductIds());

            promotion.setProducts(products);
        }

        return promotionRepository.save(promotion);
    }

    public PromotionResponseDTO createPromotionDTO(CreatePromotionDTO dto) {
        Promotion promotion = createPromotion(dto);
        return convertToResponseDTO(promotion);
    }

    public List<Promotion> getCurrentPromotions() {

        return promotionRepository.findCurrentPromotions(LocalDate.now());

    }

    /**
     * Retorna promoções ativas como DTO para evitar problemas de serialização
     */
    public List<PromotionCurrentDTO> getCurrentPromotionsDTO() {
        try {
            List<Object[]> results = promotionRepository.findCurrentPromotionsWithProductIds(LocalDate.now());
            
            if (results == null || results.isEmpty()) {
                return List.of();
            }
            
            return results.stream()
                    .map(this::convertArrayToCurrentDTO)
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("❌ ERRO ao buscar promoções: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    public Page<PromotionCurrentDTO> getCurrentPromotionsDTO(Pageable pageable) {
        List<PromotionCurrentDTO> promotions = getCurrentPromotionsDTO();
        int startIndex = (int) pageable.getOffset();
        int endIndex = Math.min(startIndex + pageable.getPageSize(), promotions.size());

        if (startIndex >= promotions.size()) {
            return new PageImpl<>(List.of(), pageable, promotions.size());
        }

        return new PageImpl<>(promotions.subList(startIndex, endIndex), pageable, promotions.size());
    }

    /**
     * Busca uma promoção por ID
     */
    public Promotion getPromotionById(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Promoção não encontrada com ID: " + id));
    }

    /**
     * Atualiza uma promoção existente
     */
    public Promotion updatePromotion(Long id, UpdatePromotionDTO dto) {
        Promotion promotion = getPromotionById(id);

        if (dto.getTitle() != null) {
            promotion.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            promotion.setDescription(dto.getDescription());
        }
        if (dto.getDiscountPercentage() != null) {
            promotion.setDiscountPercentage(dto.getDiscountPercentage());
        }
        if (dto.getDiscountAmount() != null) {
            promotion.setDiscountAmount(dto.getDiscountAmount());
        }
        if (dto.getStartDate() != null) {
            promotion.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            promotion.setEndDate(dto.getEndDate());
        }
        if (dto.getIsActive() != null) {
            promotion.setIsActive(dto.getIsActive());
        }
        if (dto.getProductIds() != null && !dto.getProductIds().isEmpty()) {
            List<Product> products = productRepository.findAllById(dto.getProductIds());
            promotion.setProducts(products);
        }

        return promotionRepository.save(promotion);
    }

    public PromotionResponseDTO updatePromotionDTO(Long id, UpdatePromotionDTO dto) {
        Promotion promotion = updatePromotion(id, dto);
        return convertToResponseDTO(promotion);
    }

    /**
     * Deleta uma promoção por ID
     */
    public void deletePromotion(Long id) {
        Promotion promotion = getPromotionById(id);
        promotionRepository.delete(promotion);
    }

    /**
     * Converte uma entidade Promotion para PromotionResponseDTO
     */
    private PromotionResponseDTO convertToResponseDTO(Promotion promotion) {
        return PromotionResponseDTO.builder()
                .id(promotion.getId())
                .title(promotion.getTitle())
                .description(promotion.getDescription())
                .discountPercentage(promotion.getDiscountPercentage())
                .discountAmount(promotion.getDiscountAmount())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .isActive(promotion.getIsActive())
                .createdAt(promotion.getCreatedAt())
                .updatedAt(promotion.getUpdatedAt())
                .build();
    }

    /**
     * Converte Object[] da query para PromotionCurrentDTO
     * Índices: 0=id, 1=title, 2=description, 3=discount_percentage, 4=discount_amount,
     *          5=start_date, 6=end_date, 7=is_active, 8=created_at, 9=updated_at, 10=productId
     */
    private PromotionCurrentDTO convertArrayToCurrentDTO(Object[] row) {
        try {
            if (row == null || row.length < 11) {
                System.err.println("⚠️ Row inválida: " + (row == null ? "null" : "comprimento=" + row.length));
                return null;
            }

            Long id = row[0] != null ? ((Number) row[0]).longValue() : null;
            String title = (String) row[1];
            String description = (String) row[2];
            java.math.BigDecimal discountPercentage = (java.math.BigDecimal) row[3];
            java.math.BigDecimal discountAmount = (java.math.BigDecimal) row[4];
            
            // Converter java.sql.Date para java.time.LocalDate
            java.time.LocalDate startDate = null;
            if (row[5] != null) {
                if (row[5] instanceof java.sql.Date) {
                    startDate = ((java.sql.Date) row[5]).toLocalDate();
                } else if (row[5] instanceof java.time.LocalDate) {
                    startDate = (java.time.LocalDate) row[5];
                }
            }
            
            java.time.LocalDate endDate = null;
            if (row[6] != null) {
                if (row[6] instanceof java.sql.Date) {
                    endDate = ((java.sql.Date) row[6]).toLocalDate();
                } else if (row[6] instanceof java.time.LocalDate) {
                    endDate = (java.time.LocalDate) row[6];
                }
            }
            
            Boolean isActive = row[7] != null ? (Boolean) row[7] : true;
            Long productId = row[10] != null ? ((Number) row[10]).longValue() : null;

            System.out.println("✓ Convertendo promoção: id=" + id + ", title=" + title + ", productId=" + productId);

            return PromotionCurrentDTO.builder()
                    .id(id)
                    .title(title)
                    .description(description)
                    .discountPercentage(discountPercentage)
                    .discountAmount(discountAmount)
                    .startDate(startDate)
                    .endDate(endDate)
                    .isActive(isActive)
                    .productId(productId)
                    .build();
        } catch (Exception e) {
            System.err.println("❌ ERRO ao converter row: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}