package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.adicional.AdicionalRequestDTO;
import com.basilios.basilios.app.dto.adicional.AdicionalResponseDTO;
import com.basilios.basilios.app.dto.adicional.AdicionalUpdateDTO;
import com.basilios.basilios.app.mapper.AdicionalMapper;
import com.basilios.basilios.core.exception.BusinessException;
import com.basilios.basilios.core.exception.NotFoundException;
import com.basilios.basilios.core.model.Adicional;
import com.basilios.basilios.infra.repository.AdicionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdicionalService {

    private final AdicionalRepository adicionalRepository;
    private final AdicionalMapper adicionalMapper;

    @Transactional
    public AdicionalResponseDTO create(AdicionalRequestDTO dto) {
        if (adicionalRepository.existsByNameIgnoreCaseAndDeletedAtIsNull(dto.getName())) {
            throw new BusinessException("Já existe um adicional com o nome '" + dto.getName() + "'");
        }

        Adicional adicional = Adicional.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .subcategory(dto.getSubcategory())
                .price(dto.getPrice())
                .available(true)
                .build();

        adicional = adicionalRepository.save(adicional);
        log.info("Adicional criado: id={}, name={}", adicional.getId(), adicional.getName());
        return adicionalMapper.toResponse(adicional);
    }

    @Transactional(readOnly = true)
    public Page<AdicionalResponseDTO> listAll(Pageable pageable) {
        return adicionalRepository.findByDeletedAtIsNull(pageable)
                .map(adicionalMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AdicionalResponseDTO getById(Long id) {
        Adicional adicional = findActiveOrThrow(id);
        return adicionalMapper.toResponse(adicional);
    }

    @Transactional
    public AdicionalResponseDTO update(Long id, AdicionalUpdateDTO dto) {
        Adicional adicional = findActiveOrThrow(id);

        if (dto.getName() != null) {
            if (!adicional.getName().equalsIgnoreCase(dto.getName()) &&
                    adicionalRepository.existsByNameIgnoreCaseAndDeletedAtIsNull(dto.getName())) {
                throw new BusinessException("Já existe um adicional com o nome '" + dto.getName() + "'");
            }
            adicional.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            adicional.setDescription(dto.getDescription());
        }

        if (dto.getSubcategory() != null) {
            adicional.setSubcategory(dto.getSubcategory());
        }

        if (dto.getPrice() != null) {
            adicional.setPrice(dto.getPrice());
        }

        if (dto.getAvailable() != null) {
            adicional.setAvailable(dto.getAvailable());
        }

        adicional = adicionalRepository.save(adicional);
        log.info("Adicional atualizado: id={}", adicional.getId());
        return adicionalMapper.toResponse(adicional);
    }

    @Transactional
    public void delete(Long id) {
        Adicional adicional = findActiveOrThrow(id);
        adicional.setDeletedAt(LocalDateTime.now());
        adicionalRepository.save(adicional);
        log.info("Adicional deletado (soft): id={}", id);
    }

    private Adicional findActiveOrThrow(Long id) {
        return adicionalRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Adicional não encontrado: " + id));
    }
}
