package com.example.mediplan.admin;

import com.example.mediplan.admin.dto.AdminChangeRoleRequest;
import com.example.mediplan.admin.dto.AdminCreateUserRequest;
import com.example.mediplan.admin.dto.AdminUserDetailsDTO;
import com.example.mediplan.common.exception.BusinessRuleException;
import com.example.mediplan.user.Administrator;
import com.example.mediplan.user.Medecin;
import com.example.mediplan.user.Role;
import com.example.mediplan.user.User;
import com.example.mediplan.user.UserRepository;
import com.example.mediplan.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private AdminUserService adminUserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminUserService = new AdminUserService(userRepository, userService, new AdminUserMapper());
    }

    @Test
    void createDoctorWithoutLicenseShouldThrowBusinessRuleException() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setFullName("Dr Jean Test");
        request.setEmail("jean.test@example.com");
        request.setPassword("Secret123!");
        request.setRole(Role.MEDECIN);
        request.setSpecialty("Cardiologie");

        assertThatThrownBy(() -> adminUserService.createUser(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("licence");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createDoctorShouldNormalizeEmailAndPersist() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setFullName("Dr Jeanne Martin");
        request.setEmail("Dr.Martin@Example.COM");
        request.setPassword("Secret123!");
        request.setRole(Role.MEDECIN);
        request.setSpecialty("Dermatologie");
        request.setLicenseNumber("LIC-0001");
        request.setYearsOfExperience(5);

        when(userService.hashPassword("Secret123!")).thenReturn("HASH");
        when(userRepository.findByEmailIgnoreCase("dr.martin@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByLicenseNumber("LIC-0001")).thenReturn(Optional.empty());
        when(userRepository.save(any(Medecin.class))).thenAnswer(invocation -> {
            Medecin medecin = invocation.getArgument(0);
            medecin.setId("user-id");
            medecin.setCreatedAt(Instant.parse("2024-01-01T10:15:30Z"));
            return medecin;
        });

        AdminUserDetailsDTO dto = adminUserService.createUser(request);

        verify(userRepository).save(userCaptor.capture());
        User persisted = userCaptor.getValue();
        assertThat(persisted.getEmail()).isEqualTo("dr.martin@example.com");
        assertThat(persisted.getPasswordHash()).isEqualTo("HASH");
        assertThat(persisted.getRole()).isEqualTo(Role.MEDECIN);
        assertThat(persisted.getLicenseNumber()).isEqualTo("LIC-0001");

        assertThat(dto.getId()).isEqualTo("user-id");
        assertThat(dto.getEmail()).isEqualTo("dr.martin@example.com");
        assertThat(dto.getRole()).isEqualTo(Role.MEDECIN);
        assertThat(dto.getLicenseNumber()).isEqualTo("LIC-0001");
    }

    @Test
    void changeRoleToMedecinWithoutLicenseShouldFail() {
        Administrator user = Administrator.builder()
                .id("admin-1")
                .fullName("Admin Test")
                .email("admin@test.com")
                .role(Role.ADMIN)
                .emailVerified(true)
                .provider("LOCAL")
                .specialty("Cardiologie")
                .build();
        when(userRepository.findById("admin-1")).thenReturn(Optional.of(user));

        AdminChangeRoleRequest request = new AdminChangeRoleRequest();
        request.setRole(Role.MEDECIN);

        assertThatThrownBy(() -> adminUserService.changeRole("admin-1", request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("licence");
    }
}
