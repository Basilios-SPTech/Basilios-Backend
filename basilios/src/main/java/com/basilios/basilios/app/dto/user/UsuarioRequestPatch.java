package com.basilios.basilios.app.dto.user;
import jakarta.validation.constraints.Email;
import java.util.Set;

    public class UsuarioRequestPatch {

        private String nomeUsuario;

        @Email(message = "Email inválido")
        private String email;

        private String telefone;

        // Ex.: ["ROLE_ADMIN", "ROLE_FUNCIONARIO"]
        private Set<String> roles;

        public String getNomeUsuario() {
            return nomeUsuario;
        }

        public void setNomeUsuario(String nomeUsuario) {
            this.nomeUsuario = nomeUsuario;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getTelefone() {
            return telefone;
        }

        public void setTelefone(String telefone) {
            this.telefone = telefone;
        }

        public Set<String> getRoles() {
            return roles;
        }

        public void setRoles(Set<String> roles) {
            this.roles = roles;
        }
    }

