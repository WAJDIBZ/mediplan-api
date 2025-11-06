package com.example.mediplan.admin.dto;

import com.example.mediplan.user.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminChangeRoleRequest {

    @NotNull(message = "Le rôle est obligatoire")
    private Role role;
}
