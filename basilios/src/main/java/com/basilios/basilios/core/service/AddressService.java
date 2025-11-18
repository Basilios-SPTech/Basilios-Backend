package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.endereco.AddressRequestDTO;
import com.basilios.basilios.app.dto.endereco.AddressResponseDTO;
import com.basilios.basilios.core.exception.BusinessException;
import com.basilios.basilios.core.exception.NotFoundException;
import com.basilios.basilios.core.model.Address;
import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.infra.repository.AddressRepository;
import com.basilios.basilios.infra.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    // ========== LISTAGEM ==========

    /**
     * Lista TODOS os endereços do banco
     */
    @Transactional(readOnly = true)
    public List<AddressResponseDTO> findAllAddress() {
        return addressRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca endereço por ID
     */
    @Transactional(readOnly = true)
    public AddressResponseDTO findById(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Endereço não encontrado: " + id));

        return toResponse(address);
    }

    /**
     * Lista todos os endereços de um usuário específico
     */
    @Transactional(readOnly = true)
    public List<AddressResponseDTO> findAllByUserId(Long usuarioId) {
        Usuario usuario = findUsuarioOrThrow(usuarioId);

        return addressRepository.findByUsuarioAndDeletedAtIsNull(usuario)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca endereço principal do usuário autenticado
     */
    @Transactional(readOnly = true)
    public AddressResponseDTO getPrincipalAddress() {
        Usuario usuario = usuarioService.getCurrentUsuario();

        Address address = addressRepository.findPrincipalByUsuario(usuario)
                .orElseThrow(() -> new NotFoundException("Usuário não possui endereço principal"));

        return toResponse(address);
    }

    // ========== CRIAÇÃO ==========

    /**
     * Cria novo endereço para o usuário autenticado
     */
    @Transactional
    public AddressResponseDTO createAddress(AddressRequestDTO request) {
        Usuario usuario = usuarioService.getCurrentUsuario();

        Address address = buildAddressFromRequest(request, usuario);
        address = addressRepository.save(address);

        // Se é o primeiro endereço, define como principal automaticamente
        if (addressRepository.countByUsuarioAndDeletedAtIsNull(usuario) == 1) {
            setAddressAsPrincipal(address, usuario);
        }

        return toResponse(address);
    }

    // ========== ATUALIZAÇÃO ==========

    /**
     * Atualiza endereço existente
     */
    @Transactional
    public AddressResponseDTO updateAddress(Long id, AddressRequestDTO request) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Endereço não encontrado: " + id));

        updateAddressFields(address, request);
        address = addressRepository.save(address);

        return toResponse(address);
    }

    // ========== DELEÇÃO ==========

    /**
     * Deleta endereço (soft delete)
     */
    @Transactional
    public void deleteAddress(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Endereço não encontrado: " + id));

        Usuario usuario = address.getUsuario();
        validateCanDeleteAddress(usuario);

        // Se é o endereço principal, define outro como principal antes de deletar
        if (isPrincipalAddress(address, usuario)) {
            setNextAddressAsPrincipal(usuario, address.getIdAddress());
        }

        address.setDeletedAt(LocalDateTime.now());
        addressRepository.save(address);
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Busca usuário ou lança exceção
     */
    private Usuario findUsuarioOrThrow(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado: " + usuarioId));
    }

    /**
     * Constrói Address a partir do DTO
     */
    private Address buildAddressFromRequest(AddressRequestDTO request, Usuario usuario) {
        return Address.builder()
                .usuario(usuario)
                .rua(request.getRua())
                .numero(request.getNumero())
                .bairro(request.getBairro())
                .cep(normalizeCep(request.getCep()))
                .cidade(request.getCidade())
                .estado(request.getEstado())
                .complemento(request.getComplemento())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
    }

    /**
     * Atualiza campos do endereço a partir do DTO
     */
    private void updateAddressFields(Address address, AddressRequestDTO request) {
        address.setRua(request.getRua());
        address.setNumero(request.getNumero());
        address.setBairro(request.getBairro());
        address.setCep(normalizeCep(request.getCep()));
        address.setCidade(request.getCidade());
        address.setEstado(request.getEstado());
        address.setComplemento(request.getComplemento());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
    }

    /**
     * Define endereço como principal
     */
    private void setAddressAsPrincipal(Address address, Usuario usuario) {
        usuario.setAddressPrincipal(address);
        usuarioRepository.save(usuario);
    }

    /**
     * Verifica se endereço é o principal do usuário
     */
    private boolean isPrincipalAddress(Address address, Usuario usuario) {
        return usuario.getAddressPrincipal() != null &&
                usuario.getAddressPrincipal().getIdAddress().equals(address.getIdAddress());
    }

    /**
     * Define próximo endereço disponível como principal
     */
    private void setNextAddressAsPrincipal(Usuario usuario, Long excludeAddressId) {
        Address newPrincipal = addressRepository.findByUsuarioAndDeletedAtIsNull(usuario)
                .stream()
                .filter(a -> !a.getIdAddress().equals(excludeAddressId))
                .findFirst()
                .orElse(null);

        usuario.setAddressPrincipal(newPrincipal);
        usuarioRepository.save(usuario);
    }

    /**
     * Valida se endereço pode ser deletado
     */
    private void validateCanDeleteAddress(Usuario usuario) {
        long activeCount = addressRepository.countByUsuarioAndDeletedAtIsNull(usuario);

        if (activeCount <= 1) {
            throw new BusinessException("Não é possível deletar o único endereço ativo");
        }
    }

    /**
     * Normaliza CEP removendo hífen
     * 12345-678 → 12345678
     */
    private String normalizeCep(String cep) {
        if (cep == null) {
            return null;
        }
        return cep.replaceAll("[^0-9]", "");
    }

    /**
     * Converte Address para AddressResponseDTO
     */
    private AddressResponseDTO toResponse(Address address) {
        Usuario usuario = address.getUsuario();

        return AddressResponseDTO.builder()
                .id(address.getIdAddress())
                .rua(address.getRua())
                .numero(address.getNumero())
                .bairro(address.getBairro())
                .cep(formatCep(address.getCep()))
                .cidade(address.getCidade())
                .estado(address.getEstado())
                .complemento(address.getComplemento())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .enderecoCompleto(address.getEnderecoCompleto())
                .isPrincipal(isPrincipalAddress(address, usuario))
                .createdAt(address.getCreatedAt())
                .build();
    }

    /**
     * Formata CEP para exibição
     * 12345678 → 12345-678
     */
    private String formatCep(String cep) {
        if (cep == null || cep.length() != 8) {
            return cep;
        }
        return cep.substring(0, 5) + "-" + cep.substring(5);
    }

    /**
     * Define um endereço como principal para o usuário autenticado
     */
    @Transactional
    public AddressResponseDTO setAsPrincipal(Long addressId) {
        Usuario usuario = usuarioService.getCurrentUsuario();

        Address address = addressRepository.findByIdAddressAndUsuario(addressId, usuario)
                .orElseThrow(() -> new NotFoundException(
                        "Endereço não encontrado ou não pertence ao usuário: " + addressId));

        setAddressAsPrincipal(address, usuario);

        return toResponse(address);
    }
}