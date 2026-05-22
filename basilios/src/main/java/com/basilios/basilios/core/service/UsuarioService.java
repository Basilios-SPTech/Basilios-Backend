package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.user.UsuarioListarDTO;
import com.basilios.basilios.app.dto.user.UsuarioProfileResponse;
import com.basilios.basilios.app.dto.user.UsuarioRequestPatch;
import com.basilios.basilios.app.mapper.UsuarioMapper;
import com.basilios.basilios.core.enums.RoleEnum;
import com.basilios.basilios.core.exception.BusinessException;
import com.basilios.basilios.core.exception.NotFoundException;
import com.basilios.basilios.core.model.Address;
import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.infra.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Obtém o usuário autenticado no contexto de segurança
     */
    @Transactional(readOnly = true)
    public Usuario getCurrentUsuario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    }

    /**
     * Busca usuário por ID
     */
    @Transactional(readOnly = true)
    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado: " + id));
    }

    /**
     * Busca usuário por email
     */
    @Transactional(readOnly = true)
    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado: " + email));
    }


    /**
     * Busca usuário por CPF
     */
    @Transactional(readOnly = true)
    public Usuario findByCpf(String cpf) {
        return usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado com CPF: " + cpf));
    }

    /**
     * Lista todos os usuários ativos
     */
    @Transactional(readOnly = true)
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    /**
     * Lista usuários com paginação
     */
    @Transactional(readOnly = true)
    public Page<Usuario> findAll(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    /**
     * Adiciona role ao usuário
     */
    @Transactional
    public Usuario addRole(Long usuarioId, RoleEnum role) {
        Usuario usuario = findById(usuarioId);

        if (usuario.hasRole(role)) {
            throw new BusinessException("Usuário já possui a role: " + role.name());
        }

        usuario.getRoles().add(role);
        return usuarioRepository.save(usuario);
    }

    /**
     * Remove role do usuário
     */
    @Transactional
    public Usuario removeRole(Long usuarioId, RoleEnum role) {
        Usuario usuario = findById(usuarioId);

        if (!usuario.hasRole(role)) {
            throw new BusinessException("Usuário não possui a role: " + role.name());
        }

        // Não permite remover ROLE_CLIENTE se for a única role
        if (role == RoleEnum.ROLE_CLIENTE && usuario.getRoles().size() == 1) {
            throw new BusinessException("Não é possível remover ROLE_CLIENTE se for a única role do usuário");
        }

        usuario.getRoles().remove(role);
        return usuarioRepository.save(usuario);
    }

    /**
     * Adiciona endereço ao usuário
     */
    @Transactional
    public Usuario addAddress(Long usuarioId, Address address) {
        Usuario usuario = findById(usuarioId);

        usuario.getAddresses().add(address);
        address.setUsuario(usuario);

        // Se for o primeiro endereço, define como principal
        if (usuario.getAddressPrincipal() == null) {
            usuario.setAddressPrincipal(address);
        }

        return usuarioRepository.save(usuario);
    }

    /**
     * Remove endereço do usuário
     */
    @Transactional
    public Usuario removeAddress(Long usuarioId, Address address) {
        Usuario usuario = findById(usuarioId);

        usuario.getAddresses().remove(address);
        address.setUsuario(null);

        // Se removeu o endereço principal, define outro como principal
        if (address.equals(usuario.getAddressPrincipal()) && !usuario.getAddresses().isEmpty()) {
            usuario.setAddressPrincipal(usuario.getAddresses().get(0));
        } else if (usuario.getAddresses().isEmpty()) {
            usuario.setAddressPrincipal(null);
        }

        return usuarioRepository.save(usuario);
    }

    /**
     * Define endereço principal do usuário
     */
    @Transactional
    public Usuario setEnderecoPrincipal(Long usuarioId, Long addressId) {
        Usuario usuario = findById(usuarioId);

        Address address = usuario.getAddresses().stream()
                .filter(a -> a.getIdAddress().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Endereço não encontrado para este usuário"));

        usuario.setAddressPrincipal(address);
        return usuarioRepository.save(usuario);
    }

    /**
     * Atualiza parcialmente os dados do usuário (PATCH)
     * Não permite alterar CPF e data de nascimento
     */
    @Transactional
    public UsuarioProfileResponse updateUsuarioPatch(Long id, UsuarioRequestPatch dto) {
        Usuario usuario = findById(id);

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            String novoEmail = dto.getEmail().trim();
            if (!usuario.getEmail().equalsIgnoreCase(novoEmail) && usuarioRepository.existsByEmail(novoEmail)) {
                throw new BusinessException("Email já cadastrado");
            }
            usuario.setEmail(novoEmail);
        }

        if (dto.getNomeUsuario() != null && !dto.getNomeUsuario().isBlank()) {
            String novoNome = dto.getNomeUsuario().trim();
            if (!usuario.getNomeUsuario().equalsIgnoreCase(novoNome) && usuarioRepository.existsByNomeUsuario(novoNome)) {
                throw new BusinessException("Nome de usuário já existe");
            }
            usuario.setNomeUsuario(novoNome);
        }

        if (dto.getTelefone() != null && !dto.getTelefone().isBlank()) {
            usuario.setTelefone(dto.getTelefone().trim());
        }

        usuarioRepository.save(usuario);
        return UsuarioMapper.toProfileResponse(usuario);
    }

    /**
     * Soft delete do usuário (desativa)
     * Retorna DTO de listagem
     */
    @Transactional
    public UsuarioListarDTO deleteUsuario(Long id) {
        Usuario usuario = findById(id);
        usuario.softDelete();
        usuarioRepository.save(usuario);
        return UsuarioMapper.toListarDTO(usuario);
    }

    /**
     * Conta total de usuários ativos
     */
    @Transactional(readOnly = true)
    public long countActiveUsuarios() {
        return usuarioRepository.countByEnabledTrue();
    }

    /**
     * Lista usuários por role
     */
    @Transactional(readOnly = true)
    public List<Usuario> findByRole(RoleEnum role) {
        return usuarioRepository.findByRolesContaining(role);
    }

    /**
     * Verifica se usuário tem permissão de funcionário
     */
    @Transactional(readOnly = true)
    public boolean isFuncionario(Long usuarioId) {
        Usuario usuario = findById(usuarioId);
        return usuario.hasRole(RoleEnum.ROLE_FUNCIONARIO);
    }


    @Transactional
    public UsuarioProfileResponse updateRole(Long id, Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos uma role");
        }

        Usuario usuario = findById(id);

        List<RoleEnum> novasRoles = roles.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toUpperCase)
            .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(RoleEnum::valueOf)
            .distinct()
            .collect(Collectors.toCollection(ArrayList::new));

        usuario.setRoles(novasRoles);

        Usuario salvo = usuarioRepository.save(usuario);
        return UsuarioMapper.toProfileResponse(salvo);
    }

    @Transactional
    public UsuarioProfileResponse updateRole(Long id, String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role inválida");
        }
        return updateRole(id, Set.of(role));
    }
}