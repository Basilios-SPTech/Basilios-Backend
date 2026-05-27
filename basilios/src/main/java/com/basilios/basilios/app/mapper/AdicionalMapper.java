package com.basilios.basilios.app.mapper;

import com.basilios.basilios.app.dto.adicional.AdicionalResponseDTO;
import com.basilios.basilios.core.model.Adicional;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdicionalMapper {

    public AdicionalResponseDTO toResponse(Adicional adicional) {
        if (adicional == null) {
            return null;
        }

        return AdicionalResponseDTO.builder()
                .id(adicional.getId())
                .name(adicional.getName())
                .description(adicional.getDescription())
                .subcategory(adicional.getSubcategory() != null ? adicional.getSubcategory().getDisplayName() : null)
                .price(adicional.getPrice())
                .available(adicional.getAvailable())
                .createdAt(adicional.getCreatedAt())
                .updatedAt(adicional.getUpdatedAt())
                .build();
    }

    public List<AdicionalResponseDTO> toResponseList(List<Adicional> adicionais) {
        if (adicionais == null) {
            return List.of();
        }
        return adicionais.stream()
                .map(this::toResponse)
                .toList();
    }
}
