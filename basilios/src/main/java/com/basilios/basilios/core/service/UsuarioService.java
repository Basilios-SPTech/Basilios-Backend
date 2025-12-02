package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.user.UsuarioListarDTO;
import com.basilios.basilios.app.dto.user.UsuarioProfileResponse;
import com.basilios.basilios.core.enums.RoleEnum;
import com.basilios.basilios.core.exception.BusinessException;
import com.basilios.basilios.core.exception.NotFoundException;
import com.basilios.basilios.core.model.Address;
import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.infra.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

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
    public UsuarioProfileResponse updateUsuarioPatch(Long id, UsuarioProfileResponse dto) {
        Usuario usuario = findById(id);

        // Validar email único (se mudou)
        if (!usuario.getEmail().equals(dto.getEmail()) && usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Email já cadastrado");
        }
        // Validar nomeUsuario único (se mudou)
        if (!usuario.getNomeUsuario().equals(dto.getNomeUsuario()) && usuarioRepository.existsByNomeUsuario(dto.getNomeUsuario())) {
            throw new BusinessException("Nome de usuário já existe");
        }
        // Atualizar apenas campos permitidos
        usuario.setNomeUsuario(dto.getNomeUsuario());
        usuario.setEmail(dto.getEmail());
        usuario.setTelefone(dto.getTelefone());
        usuarioRepository.save(usuario);
        // Retornar DTO atualizado
        return UsuarioProfileResponse.builder()
                .id(usuario.getId())
                .nomeUsuario(usuario.getNomeUsuario())
                .email(usuario.getEmail())
                .cpf(usuario.getCpf())
                .telefone(usuario.getTelefone())
                .dataNascimento(usuario.getDataNascimento())
                .roles(new java.util.HashSet<>(usuario.getRoles()))
                .enabled(usuario.isAtivo())
                .createdAt(usuario.getCreatedAt())
                .build();
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
        // Retornar DTO de listagem
        UsuarioListarDTO dto = new UsuarioListarDTO();
        dto.setNomeUsuario(usuario.getNomeUsuario());
        dto.setEmail(usuario.getEmail());
        dto.setCpf(usuario.getCpf());
        dto.setTelefone(usuario.getTelefone());
        dto.setDataNascimento(usuario.getDataNascimento());
        return dto;
    }

    /**
     * Atualiza dados básicos do usuário usando DTO
     */
    @Transactional
    public UsuarioProfileResponse updateUsuario(Long id, UsuarioProfileResponse dto) {
        Usuario usuario = findById(id);
        // Validar email único (se mudou)
        if (!usuario.getEmail().equals(dto.getEmail()) && usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Email já cadastrado");
        }
        // Validar nomeUsuario único (se mudou)
        if (!usuario.getNomeUsuario().equals(dto.getNomeUsuario()) && usuarioRepository.existsByNomeUsuario(dto.getNomeUsuario())) {
            throw new BusinessException("Nome de usuário já existe");
        }
        // Atualizar apenas campos permitidos
        usuario.setNomeUsuario(dto.getNomeUsuario());
        usuario.setEmail(dto.getEmail());
        usuario.setTelefone(dto.getTelefone());
        usuarioRepository.save(usuario);
        // Retornar DTO atualizado
        return UsuarioProfileResponse.builder()
                .id(usuario.getId())
                .nomeUsuario(usuario.getNomeUsuario())
                .email(usuario.getEmail())
                .cpf(usuario.getCpf())
                .telefone(usuario.getTelefone())
                .dataNascimento(usuario.getDataNascimento())
                .roles(new java.util.HashSet<>(usuario.getRoles()))
                .enabled(usuario.isAtivo())
                .createdAt(usuario.getCreatedAt())
                .build();
    }

    /**
     * Desativa usuário (soft delete) - deprecated, use deleteUsuario
     */
    @Transactional
    @Deprecated
    public void desativarUsuario(Long id) {
        Usuario usuario = findById(id);
        usuario.softDelete();
        usuarioRepository.save(usuario);
    }

    /**
     * Conta total de usuários ativos
     */
    @Transactional(readOnly = true)
    public long countActiveUsuarios() {
        return usuarioRepository.findAll().stream()
                .filter(Usuario::isAtivo)
                .count();
    }

    /**
     * Lista usuários por role
     */
    @Transactional(readOnly = true)
    public List<Usuario> findByRole(RoleEnum role) {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.hasRole(role))
                .toList();
    }

    /**
     * Verifica se usuário tem permissão de funcionário
     */
    @Transactional(readOnly = true)
    public boolean isFuncionario(Long usuarioId) {
        Usuario usuario = findById(usuarioId);
        return usuario.hasRole(RoleEnum.ROLE_FUNCIONARIO);
    }
}