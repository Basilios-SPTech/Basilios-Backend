package com.basilios.basilios.app.mapper;

import com.basilios.basilios.app.dto.user.UsuarioListarDTO;
import com.basilios.basilios.app.dto.user.UsuarioProfileResponse;
import com.basilios.basilios.core.model.Usuario;

public class UsuarioMapper {

    public static UsuarioProfileResponse toProfileResponse(Usuario usuario) {
        return UsuarioProfileResponse.builder()
                .id(usuario.getId())
                .nomeUsuario(usuario.getNomeUsuario())
                .email(usuario.getEmail())
                .cpf(mascaraCpf(usuario.getCpf()))
                .telefone(usuario.getTelefone())
                .dataNascimento(usuario.getDataNascimento())
                .createdAt(usuario.getCreatedAt())
                .build();
    }

    public static UsuarioListarDTO toListarDTO(Usuario usuario) {
        UsuarioListarDTO dto = new UsuarioListarDTO();
        dto.setId(usuario.getId());
        dto.setNomeUsuario(usuario.getNomeUsuario());
        dto.setEmail(usuario.getEmail());
        dto.setCpf(mascaraCpf(usuario.getCpf()));
        dto.setTelefone(usuario.getTelefone());
        dto.setDataNascimento(usuario.getDataNascimento());
        return dto;
    }

    /**
     * Mascara o CPF para exibicao: ***.456.789-01
     */
    private static String mascaraCpf(String cpf) {
        if (cpf == null) return null;
        String digits = cpf.replaceAll("[^0-9]", "");
        if (digits.length() != 11) return "***.***.***-**";
        return "***" + "." + digits.substring(3, 6) + "." + digits.substring(6, 9) + "-" + digits.substring(9);
    }
}
