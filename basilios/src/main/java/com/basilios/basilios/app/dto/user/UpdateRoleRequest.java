package com.basilios.basilios.app.dto.user;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public class UpdateRoleRequest {

    @NotEmpty(message = "Informe ao menos uma role")
    private Set<String> roles;

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
