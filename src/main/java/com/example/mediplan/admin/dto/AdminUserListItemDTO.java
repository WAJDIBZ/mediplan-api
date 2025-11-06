package com.example.mediplan.admin.dto;

import com.example.mediplan.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserListItemDTO {

    private String id;
    private String fullName;
    private String email;
    private Role role;
    private boolean active;
    private String provider;
    private Instant createdAt;
}
