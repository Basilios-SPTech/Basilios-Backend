package com.basilios.basilios.app.mapper;

import com.basilios.basilios.app.dto.user.UsuarioListarDTO;
import com.basilios.basilios.app.dto.user.UsuarioLoginDTO;
import com.basilios.basilios.app.dto.user.UsuarioRegisterDTO;
import com.basilios.basilios.app.dto.user.UsuarioTokenDTO;
import com.basilios.basilios.core.model.Usuario;

public class UsuarioMapper {

    public static Usuario of(UsuarioRegisterDTO dto) {
        return Usuario.builder()
                .nomeUsuario(dto.getNomeUsuario())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .cpf(dto.getCpf())
                .telefone(dto.getTelefone())
                .dataNascimento(dto.getDataNascimento())
                .build();
    }

    public static Usuario of(UsuarioLoginDTO dto) {
        return Usuario.builder()
                .email(dto.getEmail())
                .password(dto.getPassword())
                .build();
    }

    public static UsuarioTokenDTO of(UsuarioTokenDTO dto) {
        return UsuarioTokenDTO.builder()
                .id(dto.getId())
                .nomeUsuario(dto.getNomeUsuario())
                .email(dto.getEmail())
                .token(dto.getToken())
                .build();
    }

    public static UsuarioListarDTO of(UsuarioListarDTO dto) {
        UsuarioListarDTO listarDTO = new UsuarioListarDTO();
        listarDTO.setNomeUsuario(dto.getNomeUsuario());
        listarDTO.setEmail(dto.getEmail());
        listarDTO.setCpf(dto.getCpf());
        listarDTO.setTelefone(dto.getTelefone());
        listarDTO.setDataNascimento(dto.getDataNascimento());
        return listarDTO;
    }
}
