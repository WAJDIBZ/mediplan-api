package com.example.mediplan.admin;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.mediplan.admin.dto.AdminChangeRoleRequest;
import com.example.mediplan.admin.dto.AdminUserDetailsDTO;
import com.example.mediplan.common.exception.BusinessRuleException;
import com.example.mediplan.common.exception.ResourceConflictException;
import com.example.mediplan.user.Administrator;
import com.example.mediplan.user.Medecin;
import com.example.mediplan.user.Role;
import com.example.mediplan.user.UserRepository;
import com.example.mediplan.user.UserService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private AdminUserMapper mapper;

    @InjectMocks
    private AdminUserService adminUserService;

    @Test
    void changeRoleShouldFailWhenSpecialtyMissing() {
        Administrator admin = Administrator.builder()
                .id("user-1")
                .email("user@example.com")
                .fullName("Utilisateur Test")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findById("user-1")).thenReturn(Optional.of(admin));
        when(mapper.toAddress(any())).thenReturn(null);

        AdminChangeRoleRequest request = AdminChangeRoleRequest.builder()
                .role(Role.MEDECIN)
                .licenseNumber("LIC-999")
                .build();

        assertThrows(BusinessRuleException.class, () -> adminUserService.changeRole("user-1", request));
    }

    @Test
    void changeRoleShouldFailWhenLicenseAlreadyUsed() {
        Administrator admin = Administrator.builder()
                .id("user-1")
                .email("user@example.com")
                .fullName("Utilisateur Test")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findById("user-1")).thenReturn(Optional.of(admin));
        when(mapper.toAddress(any())).thenReturn(null);
        when(mapper.toDetails(any())).thenReturn(AdminUserDetailsDTO.builder().build());

        Medecin existingMed = Medecin.builder()
                .id("user-2")
                .email("medecin@example.com")
                .role(Role.MEDECIN)
                .licenseNumber("LIC-777")
                .build();

        when(userService.findByLicenseNumber("LIC-777"))
                .thenReturn(Optional.of(existingMed));

        AdminChangeRoleRequest request = AdminChangeRoleRequest.builder()
                .role(Role.MEDECIN)
                .specialty("Cardiologie")
                .licenseNumber("LIC-777")
                .build();

        assertThrows(ResourceConflictException.class, () -> adminUserService.changeRole("user-1", request));
    }
}
