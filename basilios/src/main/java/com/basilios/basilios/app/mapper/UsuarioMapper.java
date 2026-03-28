package com.basilios.basilios.app.mapper;

import com.basilios.basilios.app.dto.user.UsuarioListarDTO;
import com.basilios.basilios.app.dto.user.UsuarioProfileResponse;
import com.basilios.basilios.core.model.Usuario;

import java.util.HashSet;

public class UsuarioMapper {

    public static UsuarioProfileResponse toProfileResponse(Usuario usuario) {
        return UsuarioProfileResponse.builder()
                .id(usuario.getId())
                .nomeUsuario(usuario.getNomeUsuario())
                .email(usuario.getEmail())
                .cpf(usuario.getCpf())
                .telefone(usuario.getTelefone())
                .dataNascimento(usuario.getDataNascimento())
                .roles(new HashSet<>(usuario.getRoles()))
                .enabled(usuario.isAtivo())
                .createdAt(usuario.getCreatedAt())
                .build();
    }

    public static UsuarioListarDTO toListarDTO(Usuario usuario) {
        UsuarioListarDTO dto = new UsuarioListarDTO();
        dto.setId(usuario.getId());
        dto.setNomeUsuario(usuario.getNomeUsuario());
        dto.setEmail(usuario.getEmail());
        dto.setCpf(usuario.getCpf());
        dto.setTelefone(usuario.getTelefone());
        dto.setDataNascimento(usuario.getDataNascimento());
        return dto;
    }
}
