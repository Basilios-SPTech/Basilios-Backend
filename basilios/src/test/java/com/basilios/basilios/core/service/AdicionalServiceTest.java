package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.adicional.AdicionalRequestDTO;
import com.basilios.basilios.app.dto.adicional.AdicionalResponseDTO;
import com.basilios.basilios.app.dto.adicional.AdicionalUpdateDTO;
import com.basilios.basilios.app.mapper.AdicionalMapper;
import com.basilios.basilios.core.enums.AdicionalSubcategory;
import com.basilios.basilios.core.exception.BusinessException;
import com.basilios.basilios.core.exception.NotFoundException;
import com.basilios.basilios.core.model.Adicional;
import com.basilios.basilios.infra.repository.AdicionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AdicionalService")
class AdicionalServiceTest {

    @Mock
    private AdicionalRepository adicionalRepository;

    @Mock
    private AdicionalMapper adicionalMapper;

    @InjectMocks
    private AdicionalService adicionalService;

    private Adicional adicional;
    private AdicionalRequestDTO requestDTO;
    private AdicionalResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        adicional = Adicional.builder()
                .id(1L)
                .name("Extra Bacon")
                .description("Fatias extras de bacon")
                .subcategory(AdicionalSubcategory.BACON)
                .price(new BigDecimal("3.00"))
                .available(true)
                .build();

        requestDTO = AdicionalRequestDTO.builder()
                .name("Extra Bacon")
                .description("Fatias extras de bacon")
                .subcategory(AdicionalSubcategory.BACON)
                .price(new BigDecimal("3.00"))
                .build();

        responseDTO = AdicionalResponseDTO.builder()
                .id(1L)
                .name("Extra Bacon")
                .description("Fatias extras de bacon")
                .subcategory("Bacon")
                .price(new BigDecimal("3.00"))
                .available(true)
                .build();
    }

    // ========== CREATE ==========

    @Test
    @DisplayName("Deve criar adicional com sucesso")
    void create_DeveCriarComSucesso() {
        when(adicionalRepository.existsByNameIgnoreCaseAndDeletedAtIsNull("Extra Bacon")).thenReturn(false);
        when(adicionalRepository.save(any(Adicional.class))).thenReturn(adicional);
        when(adicionalMapper.toResponse(any(Adicional.class))).thenReturn(responseDTO);

        AdicionalResponseDTO result = adicionalService.create(requestDTO);

        assertNotNull(result);
        assertEquals("Extra Bacon", result.getName());
        verify(adicionalRepository).save(any(Adicional.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar adicional com nome duplicado")
    void create_DeveLancarExcecaoQuandoNomeDuplicado() {
        when(adicionalRepository.existsByNameIgnoreCaseAndDeletedAtIsNull("Extra Bacon")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> adicionalService.create(requestDTO));

        assertEquals("Já existe um adicional com o nome 'Extra Bacon'", ex.getMessage());
        verify(adicionalRepository, never()).save(any());
    }

    // ========== LIST ==========

    @Test
    @DisplayName("Deve listar adicionais paginados")
    void listAll_DeveRetornarPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Adicional> page = new PageImpl<>(List.of(adicional), pageable, 1);

        when(adicionalRepository.findByDeletedAtIsNull(pageable)).thenReturn(page);
        when(adicionalMapper.toResponse(any(Adicional.class))).thenReturn(responseDTO);

        Page<AdicionalResponseDTO> result = adicionalService.listAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    // ========== GET BY ID ==========

    @Test
    @DisplayName("Deve retornar adicional por ID")
    void getById_DeveRetornarAdicional() {
        when(adicionalRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(adicional));
        when(adicionalMapper.toResponse(adicional)).thenReturn(responseDTO);

        AdicionalResponseDTO result = adicionalService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando ID não existe")
    void getById_DeveLancarExcecaoQuandoNaoExiste() {
        when(adicionalRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> adicionalService.getById(99L));

        assertEquals("Adicional não encontrado: 99", ex.getMessage());
    }

    // ========== UPDATE ==========

    @Test
    @DisplayName("Deve atualizar adicional com sucesso")
    void update_DeveAtualizarComSucesso() {
        AdicionalUpdateDTO updateDTO = AdicionalUpdateDTO.builder()
                .name("Extra Bacon Crocante")
                .price(new BigDecimal("4.00"))
                .build();

        when(adicionalRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(adicional));
        when(adicionalRepository.existsByNameIgnoreCaseAndDeletedAtIsNull("Extra Bacon Crocante")).thenReturn(false);
        when(adicionalRepository.save(any(Adicional.class))).thenReturn(adicional);
        when(adicionalMapper.toResponse(any(Adicional.class))).thenReturn(responseDTO);

        AdicionalResponseDTO result = adicionalService.update(1L, updateDTO);

        assertNotNull(result);
        verify(adicionalRepository).save(any(Adicional.class));
    }

    @Test
    @DisplayName("Deve atualizar campos parcialmente (PATCH)")
    void update_DeveAtualizarParcialmente() {
        AdicionalUpdateDTO updateDTO = AdicionalUpdateDTO.builder()
                .available(false)
                .build();

        when(adicionalRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(adicional));
        when(adicionalRepository.save(any(Adicional.class))).thenReturn(adicional);
        when(adicionalMapper.toResponse(any(Adicional.class))).thenReturn(responseDTO);

        AdicionalResponseDTO result = adicionalService.update(1L, updateDTO);

        assertNotNull(result);
        verify(adicionalRepository).save(any(Adicional.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar com nome duplicado")
    void update_DeveLancarExcecaoQuandoNomeDuplicado() {
        AdicionalUpdateDTO updateDTO = AdicionalUpdateDTO.builder()
                .name("Extra Queijo")
                .build();

        when(adicionalRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(adicional));
        when(adicionalRepository.existsByNameIgnoreCaseAndDeletedAtIsNull("Extra Queijo")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> adicionalService.update(1L, updateDTO));

        assertEquals("Já existe um adicional com o nome 'Extra Queijo'", ex.getMessage());
        verify(adicionalRepository, never()).save(any());
    }

    // ========== DELETE (SOFT) ==========

    @Test
    @DisplayName("Deve fazer soft delete do adicional")
    void delete_DeveFazerSoftDelete() {
        when(adicionalRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(adicional));
        when(adicionalRepository.save(any(Adicional.class))).thenReturn(adicional);

        adicionalService.delete(1L);

        assertNotNull(adicional.getDeletedAt());
        verify(adicionalRepository).save(adicional);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao deletar ID inexistente")
    void delete_DeveLancarExcecaoQuandoNaoExiste() {
        when(adicionalRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> adicionalService.delete(99L));

        assertEquals("Adicional não encontrado: 99", ex.getMessage());
        verify(adicionalRepository, never()).save(any());
    }
}
