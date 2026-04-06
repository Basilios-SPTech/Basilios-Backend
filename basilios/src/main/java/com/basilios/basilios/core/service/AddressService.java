package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.endereco.AddressRequestDTO;
import com.basilios.basilios.app.dto.endereco.AddressResponseDTO;
import com.basilios.basilios.core.exception.NotFoundException;
import com.basilios.basilios.core.model.Address;
import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.infra.repository.AddressRepository;
import com.basilios.basilios.infra.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
     * Lista TODOS os endereços do banco com paginação
     */
    @Transactional(readOnly = true)
    public Page<AddressResponseDTO> findAllAddress(Pageable pageable) {
        return addressRepository.findAll(pageable).map(this::toResponse);
    }

    /**
     * Busca endereço por ID com regra de ownership (ou funcionário)
     */
    @Transactional(readOnly = true)
    public AddressResponseDTO findById(Long id) {
        Address address = findAddressWithOwnership(id);
        return toResponse(address);
    }

    /**
     * Lista todos os endereços de um usuário específico (DTO)
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
     * Lista todos os endereços de um usuário específico (DTO) com paginação
     */
    @Transactional(readOnly = true)
    public Page<AddressResponseDTO> findAllByUserId(Long usuarioId, Pageable pageable) {
        Usuario usuario = findUsuarioOrThrow(usuarioId);
        return addressRepository.findByUsuarioAndDeletedAtIsNull(usuario, pageable)
                .map(this::toResponse);
    }

    /**
     * Lista endereços do usuário autenticado (método esperado pelos testes)
     */
    @Transactional(readOnly = true)
    public List<AddressResponseDTO> getUserAddresses() {
        Usuario usuario = usuarioService.getCurrentUsuario();
        return addressRepository.findByUsuarioAndDeletedAtIsNullOrderByCreatedAtDescIdAddressDesc(usuario)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retorna lista de endereços ativos (entidade) para um usuário (método esperado pelos testes)
     */
    @Transactional(readOnly = true)
    public List<Address> findActiveAddressesByUserId(Long usuarioId) {
        Usuario usuario = findUsuarioOrThrow(usuarioId);
        return addressRepository.findByUsuarioAndDeletedAtIsNull(usuario);
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

        return toResponse(address);
    }

    // ========== ATUALIZAÇÃO ==========

    /**
     * Atualiza endereço existente
     */
    @Transactional
    public AddressResponseDTO updateAddress(Long id, AddressRequestDTO request) {
        Address address = findAddressWithOwnership(id);

        updateAddressFields(address, request);
        address = addressRepository.save(address);

        return toResponse(address);
    }

    // ========== DELEÇÃO / RESTORE ==========

    /**
     * Deleta endereço (soft delete)
     */
    @Transactional
    public void deleteAddress(Long id) {
        Address address = findAddressWithOwnership(id);
        address.setDeletedAt(LocalDateTime.now());
        addressRepository.save(address);
    }

    /**
     * Restaura um endereço deletado (método esperado pelos testes)
     */
    @Transactional
    public AddressResponseDTO restoreAddress(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Endereço não encontrado: " + id));

        address.setDeletedAt(null);
        address = addressRepository.save(address);
        return toResponse(address);
    }

    // ========== MÉTODOS AUXILIARES / COMPATIBILIDADE ==========

    /**
     * Conta endereços ativos do usuário autenticado (método esperado pelos testes)
     */
    @Transactional(readOnly = true)
    public long countUserActiveAddresses() {
        Usuario usuario = usuarioService.getCurrentUsuario();
        return addressRepository.countByUsuarioAndDeletedAtIsNull(usuario);
    }

    /**
     * Verifica se o usuário tem algum endereço (método esperado pelos testes)
     */
    @Transactional(readOnly = true)
    public boolean hasAddresses() {
        Usuario usuario = usuarioService.getCurrentUsuario();
        return addressRepository.countByUsuarioAndDeletedAtIsNull(usuario) > 0;
    }

    /**
     * Verifica se há endereço principal para usuário autenticado (método esperado pelos testes)
     */
    @Transactional(readOnly = true)
    public boolean hasPrincipalAddress() {
        Usuario usuario = usuarioService.getCurrentUsuario();
        return addressRepository.findPrincipalByUsuario(usuario).isPresent();
    }

    /**
     * Retorna endereço principal por id (método esperado pelos testes)
     */
    @Transactional(readOnly = true)
    public AddressResponseDTO getPrincipalAddressById(Long id) {
        Address address = addressRepository.findByIdAddressAndUsuario(id, usuarioService.getCurrentUsuario())
                .orElseThrow(() -> new NotFoundException("Endereço não encontrado: " + id));
        return toResponse(address);
    }

    /**
     * Retorna um endereço do usuário autenticado por id (método esperado pelos testes)
     */
    @Transactional(readOnly = true)
    public AddressResponseDTO getAuthenticatedUserAddressById(Long id) {
        return getPrincipalAddressById(id);
    }

    // ========== MÉTODOS AUXILIARES EXISTENTES ==========

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
     * Define Address como principal (legado)
     */
    private void setAddressAsPrincipal(Address address, Usuario usuario) {
        usuario.setAddressPrincipal(address);
        usuarioRepository.save(usuario);
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
     * Normaliza CEP removendo caracteres não numéricos
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
        if (address == null) {
            throw new IllegalArgumentException("Address não pode ser nulo");
        }
        Usuario usuario = address.getUsuario();
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário do endereço não pode ser nulo");
        }
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
                .createdAt(address.getCreatedAt())
                .build();
    }

    private String formatCep(String cep) {
        if (cep == null || cep.length() != 8) return cep;
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

    /**
     * Verifica se o endereço pertence ao usuário autenticado (usado por @PreAuthorize)
     */
    @Transactional(readOnly = true)
    public boolean isOwner(Long addressId) {
        Usuario usuario = usuarioService.getCurrentUsuario();
        return addressRepository.findById(addressId)
                .map(address -> address.getUsuario().getId().equals(usuario.getId()))
                .orElse(false);
    }

    private Address findAddressWithOwnership(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Endereço não encontrado: " + id));

        if (isFuncionarioAuthenticated()) {
            return address;
        }

        Usuario usuario = usuarioService.getCurrentUsuario();
        if (!address.getUsuario().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("Acesso negado ao endereço informado");
        }

        return address;
    }

    private boolean isFuncionarioAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(a -> "ROLE_FUNCIONARIO".equals(a.getAuthority()));
    }
}
