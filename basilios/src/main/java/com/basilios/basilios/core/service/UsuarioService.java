package com.basilios.basilios.core.service;

import com.basilios.basilios.core.enums.RoleEnum;
import com.basilios.basilios.core.exception.BusinessException;
import com.basilios.basilios.core.exception.ResourceNotFoundException;
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
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    /**
     * Busca usuário por ID
     */
    @Transactional(readOnly = true)
    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }

    /**
     * Busca usuário por email
     */
    @Transactional(readOnly = true)
    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + email));
    }

    /**
     * Busca usuário por nomeUsuario
     */
    @Transactional(readOnly = true)
    public Usuario findByNomeUsuario(String nomeUsuario) {
        return usuarioRepository.findByNomeUsuario(nomeUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + nomeUsuario));
    }

    /**
     * Busca usuário por CPF
     */
    @Transactional(readOnly = true)
    public Usuario findByCpf(String cpf) {
        return usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com CPF: " + cpf));
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
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado para este usuário"));

        usuario.setAddressPrincipal(address);
        return usuarioRepository.save(usuario);
    }

    /**
     * Atualiza dados básicos do usuário
     */
    @Transactional
    public Usuario updateUsuario(Long id, Usuario dadosAtualizados) {
        Usuario usuario = findById(id);

        // Validar email único (se mudou)
        if (!usuario.getEmail().equals(dadosAtualizados.getEmail()) &&
                usuarioRepository.existsByEmail(dadosAtualizados.getEmail())) {
            throw new BusinessException("Email já cadastrado");
        }

        // Validar nomeUsuario único (se mudou)
        if (!usuario.getNomeUsuario().equals(dadosAtualizados.getNomeUsuario()) &&
                usuarioRepository.existsByNomeUsuario(dadosAtualizados.getNomeUsuario())) {
            throw new BusinessException("Nome de usuário já existe");
        }

        // Atualizar campos permitidos
        usuario.setNomeUsuario(dadosAtualizados.getNomeUsuario());
        usuario.setEmail(dadosAtualizados.getEmail());
        usuario.setTelefone(dadosAtualizados.getTelefone());

        return usuarioRepository.save(usuario);
    }

    /**
     * Desativa usuário (soft delete)
     */
    @Transactional
    public void desativarUsuario(Long id) {
        Usuario usuario = findById(id);
        usuario.softDelete();
        usuarioRepository.save(usuario);
    }

    /**
     * Reativa usuário
     */
    @Transactional
    public Usuario reativarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        if (usuario.isAtivo()) {
            throw new BusinessException("Usuário já está ativo");
        }

        usuario.restaurar();
        return usuarioRepository.save(usuario);
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