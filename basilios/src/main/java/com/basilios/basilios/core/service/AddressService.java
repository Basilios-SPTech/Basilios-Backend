package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.endereco.AddressRequestDTO;
import com.basilios.basilios.app.dto.endereco.AddressResponseDTO;
import com.basilios.basilios.core.exception.BusinessException;
import com.basilios.basilios.core.exception.ResourceNotFoundException;
import com.basilios.basilios.core.model.Address;
import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.infra.repository.EnderecoRepository;
import com.basilios.basilios.infra.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressService {

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    // ========== LISTAGEM ==========

    /**
     * Lista todos os endereços do usuário autenticado (apenas ativos)
     */
    @Transactional(readOnly = true)
    public List<AddressResponseDTO> getUserAddresses() {
        Usuario usuario = usuarioService.getCurrentUsuario();
        return usuario.getAddresses().stream()
                .filter(Address::isAtivo)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista todos os endereços de um usuário específico (admin)
     */
    @Transactional(readOnly = true)
    public List<Address> getAddressesByUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + usuarioId));

        return usuario.getAddresses().stream()
                .filter(Address::isAtivo)
                .collect(Collectors.toList());
    }

    /**
     * Busca endereço por ID
     */
    @Transactional(readOnly = true)
    public Address findById(Long id) {
        return enderecoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado: " + id));
    }

    /**
     * Busca endereço por ID do usuário autenticado (com validação de propriedade)
     */
    @Transactional(readOnly = true)
    public AddressResponseDTO getUserAddressById(Long id) {
        Usuario usuario = usuarioService.getCurrentUsuario();
        Address address = findById(id);

        validateOwnership(address, usuario);

        return toResponse(address);
    }

    /**
     * Busca endereço principal do usuário autenticado
     */
    @Transactional(readOnly = true)
    public AddressResponseDTO getPrincipalAddress() {
        Usuario usuario = usuarioService.getCurrentUsuario();

        if (usuario.getAddressPrincipal() == null) {
            throw new ResourceNotFoundException("Usuário não possui endereço principal");
        }

        return toResponse(usuario.getAddressPrincipal());
    }

    // ========== CRIAÇÃO ==========

    /**
     * Cria novo endereço para o usuário autenticado
     */
    @Transactional
    public AddressResponseDTO createAddress(AddressRequestDTO request) {
        Usuario usuario = usuarioService.getCurrentUsuario();

        // Validar CEP
        validateCep(request.getCep());

        // Criar endereço
        Address address = Address.builder()
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

        // Adicionar ao usuário
        usuario.addEndereco(address);

        // Salvar
        address = enderecoRepository.save(address);
        usuarioRepository.save(usuario);

        return toResponse(address);
    }

    // ========== ATUALIZAÇÃO ==========

    /**
     * Atualiza endereço do usuário autenticado
     */
    @Transactional
    public AddressResponseDTO updateAddress(Long id, AddressRequestDTO request) {
        Usuario usuario = usuarioService.getCurrentUsuario();
        Address address = findById(id);

        // Validar propriedade
        validateOwnership(address, usuario);

        // Validar se está ativo
        if (!address.isAtivo()) {
            throw new BusinessException("Endereço foi deletado e não pode ser atualizado");
        }

        // Validar CEP
        validateCep(request.getCep());

        // Atualizar campos
        address.setRua(request.getRua());
        address.setNumero(request.getNumero());
        address.setBairro(request.getBairro());
        address.setCep(normalizeCep(request.getCep()));
        address.setCidade(request.getCidade());
        address.setEstado(request.getEstado());
        address.setComplemento(request.getComplemento());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());

        address = enderecoRepository.save(address);

        return toResponse(address);
    }

    // ========== ENDEREÇO PRINCIPAL ==========

    /**
     * Define um endereço como principal para o usuário autenticado
     */
    @Transactional
    public AddressResponseDTO setAsPrincipal(Long addressId) {
        Usuario usuario = usuarioService.getCurrentUsuario();
        Address address = findById(addressId);

        // Validar propriedade
        validateOwnership(address, usuario);

        // Validar se está ativo
        if (!address.isAtivo()) {
            throw new BusinessException("Endereço deletado não pode ser definido como principal");
        }

        // Definir como principal
        usuario.setAddressPrincipal(address);
        usuarioRepository.save(usuario);

        return toResponse(address);
    }

    // ========== DELEÇÃO ==========

    /**
     * Deleta endereço do usuário autenticado (soft delete)
     */
    @Transactional
    public void deleteAddress(Long id) {
        Usuario usuario = usuarioService.getCurrentUsuario();
        Address address = findById(id);

        // Validar propriedade
        validateOwnership(address, usuario);

        // Validar se não é o único endereço
        long activeAddressesCount = usuario.getAddresses().stream()
                .filter(Address::isAtivo)
                .count();

        if (activeAddressesCount <= 1) {
            throw new BusinessException("Não é possível deletar o único endereço ativo");
        }

        // Verificar se é o endereço principal
        if (usuario.getAddressPrincipal() != null &&
                usuario.getAddressPrincipal().getIdEndereco().equals(id)) {
            // Definir outro endereço como principal
            Address newPrincipal = usuario.getAddresses().stream()
                    .filter(Address::isAtivo)
                    .filter(a -> !a.getIdEndereco().equals(id))
                    .findFirst()
                    .orElse(null);

            usuario.setAddressPrincipal(newPrincipal);
            usuarioRepository.save(usuario);
        }

        // Soft delete
        usuario.removeEndereco(address);
        enderecoRepository.save(address);
    }

    /**
     * Restaura endereço deletado (soft delete reversal)
     */
    @Transactional
    public AddressResponseDTO restoreAddress(Long id) {
        Usuario usuario = usuarioService.getCurrentUsuario();
        Address address = enderecoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado: " + id));

        // Validar propriedade
        validateOwnership(address, usuario);

        // Verificar se está deletado
        if (address.isAtivo()) {
            throw new BusinessException("Endereço já está ativo");
        }

        // Restaurar
        address.restaurar();
        address = enderecoRepository.save(address);

        // Se usuário não tem endereço principal, definir este
        if (usuario.getAddressPrincipal() == null) {
            usuario.setAddressPrincipal(address);
            usuarioRepository.save(usuario);
        }

        return toResponse(address);
    }

    // ========== VALIDAÇÕES ==========

    /**
     * Valida se o endereço pertence ao usuário
     */
    private void validateOwnership(Address address, Usuario usuario) {
        if (address.getUsuario() == null ||
                !address.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("Endereço não pertence ao usuário");
        }
    }

    /**
     * Valida formato do CEP
     * Aceita: 12345678 ou 12345-678
     */
    private void validateCep(String cep) {
        if (cep == null || cep.isBlank()) {
            throw new BusinessException("CEP é obrigatório");
        }

        String cepNormalizado = normalizeCep(cep);

        if (!cepNormalizado.matches("\\d{8}")) {
            throw new BusinessException("CEP inválido. Formato esperado: 12345678 ou 12345-678");
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
     * Valida coordenadas geográficas
     */
    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new BusinessException("Coordenadas (latitude/longitude) são obrigatórias");
        }

        if (latitude < -90 || latitude > 90) {
            throw new BusinessException("Latitude inválida. Deve estar entre -90 e 90");
        }

        if (longitude < -180 || longitude > 180) {
            throw new BusinessException("Longitude inválida. Deve estar entre -180 e 180");
        }
    }

    // ========== ESTATÍSTICAS ==========

    /**
     * Conta endereços ativos do usuário
     */
    @Transactional(readOnly = true)
    public long countUserActiveAddresses() {
        Usuario usuario = usuarioService.getCurrentUsuario();
        return usuario.getAddresses().stream()
                .filter(Address::isAtivo)
                .count();
    }

    /**
     * Verifica se usuário tem endereços cadastrados
     */
    @Transactional(readOnly = true)
    public boolean hasAddresses() {
        Usuario usuario = usuarioService.getCurrentUsuario();
        return usuario.getAddresses().stream()
                .anyMatch(Address::isAtivo);
    }

    /**
     * Verifica se usuário tem endereço principal definido
     */
    @Transactional(readOnly = true)
    public boolean hasPrincipalAddress() {
        Usuario usuario = usuarioService.getCurrentUsuario();
        return usuario.getAddressPrincipal() != null &&
                usuario.getAddressPrincipal().isAtivo();
    }

    // ========== CONVERSÃO PARA DTO ==========

    /**
     * Converte Address para AddressResponse
     */
    private AddressResponseDTO toResponse(Address address) {
        return AddressResponseDTO.builder()
                .id(address.getIdEndereco())
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
                .isPrincipal(isPrincipal(address))
                .createdAt(address.getCreatedAt())
                .build();
    }

    /**
     * Verifica se endereço é o principal do usuário
     */
    private boolean isPrincipal(Address address) {
        Usuario usuario = address.getUsuario();
        if (usuario == null || usuario.getAddressPrincipal() == null) {
            return false;
        }
        return usuario.getAddressPrincipal().getIdEndereco().equals(address.getIdEndereco());
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