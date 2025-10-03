package com.basilios.basilios.infra.security;

import com.basilios.basilios.core.model.Usuario;
import com.basilios.basilios.infra.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // Busca por email OU nomeUsuario (login flexível)
        Usuario usuario = usuarioRepository.findByEmailOrNomeUsuario(identifier, identifier)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + identifier));

        // Verifica se usuário está ativo
        if (!usuario.getEnabled()) {
            throw new UsernameNotFoundException("Usuário desabilitado: " + identifier);
        }



        return org.springframework.security.core.userdetails.User
                .withUsername(usuario.getEmail()) // Usa email como username principal
                .password(usuario.getPassword())
                .authorities(usuario.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getValue()))
                        .collect(Collectors.toList()))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!usuario.getEnabled())
                .build();
    }
}