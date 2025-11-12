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
     * Lista todos os endereços do usuário autenticado (apenas ativos)
     */
    @Transactional(readOnly = true)
    public List<AddressResponseDTO> getUserAddresses() {
        Usuario usuario = usuarioService.getCurrentUsuario();
        return addressRepository.findByUsuarioAndDeletedAtIsNull(usuario)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista todos os endereços de um usuário específico (admin)
     */
    @Transactional(readOnly = true)
    public List<Address> findActiveAddressesByUserId(Long usuarioId) {
        Usuario usuario = findUsuarioOrThrow(usuarioId);
        return addressRepository.findByUsuarioAndDeletedAtIsNull(usuario);
    }

    /**
     * Busca endereço por ID e valida propriedade
     */
    @Transactional(readOnly = true)
    public AddressResponseDTO findById(Long id) {
        Usuario usuario = usuarioService.getCurrentUsuario();
        Address address = findAddressByIdAndUsuarioOrThrow(id, usuario);
        return toResponse(address);
    }

    /**
     * Busca endereço do usuário autenticado por ID (valida propriedade)
     */
    @Transactional(readOnly = true)
    public AddressResponseDTO getAuthenticatedUserAddressById(Long id) {
        Usuario usuario = usuarioService.getCurrentUsuario();
        Address address = findAddressByIdAndUsuarioOrThrow(id, usuario);
        return toResponse(address);
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

    /**
     * Busca endereço principal por ID do usuário (admin)
     */
    @Transactional(readOnly = true)
    public AddressResponseDTO getPrincipalAddressById(Long usuarioId) {
        Usuario usuario = findUsuarioOrThrow(usuarioId);
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
     * Atualiza endereço do usuário autenticado
     */
    @Transactional
    public AddressResponseDTO updateAddress(Long id, AddressRequestDTO request) {
        Usuario usuario = usuarioService.getCurrentUsuario();
        Address address = findAddressByIdAndUsuarioOrThrow(id, usuario);

        updateAddressFields(address, request);

        address = addressRepository.save(address);
        return toResponse(address);
    }

    // ========== ENDEREÇO PRINCIPAL ==========

    /**
     * Define um endereço como principal para o usuário autenticado
     */
    @Transactional
    public AddressResponseDTO setAsPrincipal(Long addressId) {
        Usuario usuario = usuarioService.getCurrentUsuario();
        Address address = findAddressByIdAndUsuarioOrThrow(addressId, usuario);

        setAddressAsPrincipal(address, usuario);
        return toResponse(address);
    }

    // ========== DELEÇÃO ==========

    /**
     * Deleta endereço do usuário autenticado (soft delete)
     */
    @Transactional
    public void deleteAddress(Long id) {
        Usuario usuario = usuarioService.getCurrentUsuario();
        Address address = findAddressByIdAndUsuarioOrThrow(id, usuario);

        validateCanDeleteAddress(usuario, address);

        // Se é o endereço principal, define outro como principal
        if (isPrincipalAddress(address, usuario)) {
            setNextAddressAsPrincipal(usuario, address.getIdAddress());
        }

        address.setDeletedAt(LocalDateTime.now());
        addressRepository.save(address);
    }

    /**
     * Restaura endereço deletado (soft delete reversal)
     */
    @Transactional
    public AddressResponseDTO restoreAddress(Long id) {
        Usuario usuario = usuarioService.getCurrentUsuario();
        Address address = addressRepository.findByIdAddressAndUsuario(id, usuario)
                .orElseThrow(() -> new NotFoundException("Endereço não encontrado: " + id));

        if (address.isAtivo()) {
            throw new BusinessException("Endereço já está ativo");
        }

        address.restaurar();
        address = addressRepository.save(address);

        // Se usuário não tem endereço principal, define este
        if (!addressRepository.hasPrincipalAddress(usuario)) {
            setAddressAsPrincipal(address, usuario);
        }

        return toResponse(address);
    }

    // ========== ESTATÍSTICAS ==========

    /**
     * Conta endereços ativos do usuário
     */
    @Transactional(readOnly = true)
    public long countUserActiveAddresses() {
        Usuario usuario = usuarioService.getCurrentUsuario();
        return addressRepository.countByUsuarioAndDeletedAtIsNull(usuario);
    }

    /**
     * Verifica se usuário tem endereços cadastrados
     */
    @Transactional(readOnly = true)
    public boolean hasAddresses() {
        Usuario usuario = usuarioService.getCurrentUsuario();
        return addressRepository.countByUsuarioAndDeletedAtIsNull(usuario) > 0;
    }

    /**
     * Verifica se usuário tem endereço principal definido
     */
    @Transactional(readOnly = true)
    public boolean hasPrincipalAddress() {
        Usuario usuario = usuarioService.getCurrentUsuario();
        return addressRepository.hasPrincipalAddress(usuario);
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
     * Busca endereço por ID e usuário ou lança exceção
     */
    private Address findAddressByIdAndUsuarioOrThrow(Long id, Usuario usuario) {
        return addressRepository.findByIdAddressAndUsuario(id, usuario)
                .orElseThrow(() -> new NotFoundException("Endereço não encontrado ou não pertence ao usuário: " + id));
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
    private void validateCanDeleteAddress(Usuario usuario, Address address) {
        long activeCount = addressRepository.countByUsuarioAndDeletedAtIsNull(usuario);

        if (activeCount <= 1) {
            throw new BusinessException("Não é possível deletar o único endereço ativo");
        }
    }

    // ========== VALIDAÇÕES ==========

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

    // ========== CONVERSÃO PARA DTO ==========

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
}