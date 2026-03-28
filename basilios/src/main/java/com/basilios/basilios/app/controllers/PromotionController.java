package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.promotion.CreatePromotionDTO;
import com.basilios.basilios.app.dto.promotion.PromotionCurrentDTO;
import com.basilios.basilios.app.dto.promotion.UpdatePromotionDTO;
import com.basilios.basilios.app.dto.promotion.PromotionResponseDTO;
import com.basilios.basilios.core.service.PromotionService;
import com.basilios.basilios.core.model.Promotion;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    public ResponseEntity<PromotionResponseDTO> createPromotion(
            @RequestBody CreatePromotionDTO dto) {

        PromotionResponseDTO promotion = promotionService.createPromotionDTO(dto);

        return ResponseEntity.ok(promotion);
    }

    @GetMapping("/current")
    public ResponseEntity<Page<PromotionCurrentDTO>> getCurrentPromotions(
            @PageableDefault(size = 10) Pageable pageable) {

        Page<PromotionCurrentDTO> promotions =
                promotionService.getCurrentPromotionsDTO(pageable);

        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Promotion> getPromotionById(@PathVariable Long id) {
        Promotion promotion = promotionService.getPromotionById(id);
        return ResponseEntity.ok(promotion);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromotionResponseDTO> updatePromotion(
            @PathVariable Long id,
            @RequestBody UpdatePromotionDTO dto) {

        PromotionResponseDTO promotion = promotionService.updatePromotionDTO(id, dto);

        return ResponseEntity.ok(promotion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.noContent().build();
    }

}