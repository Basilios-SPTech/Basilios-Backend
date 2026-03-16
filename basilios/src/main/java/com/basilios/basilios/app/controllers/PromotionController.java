package com.basilios.basilios.app.controllers;

import com.basilios.basilios.app.dto.promotion.CreatePromotionDTO;
import com.basilios.basilios.app.dto.promotion.PromotionCurrentDTO;
import com.basilios.basilios.core.service.PromotionService;
import com.basilios.basilios.core.model.Promotion;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    public ResponseEntity<Promotion> createPromotion(
            @RequestBody CreatePromotionDTO dto) {

        Promotion promotion = promotionService.createPromotion(dto);

        return ResponseEntity.ok(promotion);
    }

    @GetMapping("/current")
    public ResponseEntity<List<PromotionCurrentDTO>> getCurrentPromotions() {

        List<PromotionCurrentDTO> promotions =
                promotionService.getCurrentPromotionsDTO();

        return ResponseEntity.ok(promotions);
    }

}