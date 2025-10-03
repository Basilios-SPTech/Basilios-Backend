package com.basilios.basilios.core.service;

import com.basilios.basilios.app.dto.user.UsuarioProfileResponse;
import com.basilios.basilios.core.exception.ResourceNotFoundException;
import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.infra.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * Obtém o perfil do usuário autenticado
     */
    @Transactional(readOnly = true)
    public UsuarioProfileResponse getProfile() {
        Usuario usuario = getCurrentUsuario();
        return UsuarioProfileResponse.builder()
                .id(usuario.getId())
                .nomeUsuario(usuario.getNomeUsuario())
                .email(usuario.getEmail())
                .cpf(usuario.getCpf())
                .telefone(usuario.getTelefone())
                .dataNascimento(usuario.getDataNascimento())
                .roles(usuario.getRoles())
                .enabled(usuario.getEnabled())
                .createdAt(usuario.getCreatedAt())
                .build();
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
    public void reativarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
        usuario.restaurar();
        usuarioRepository.save(usuario);
    }
}