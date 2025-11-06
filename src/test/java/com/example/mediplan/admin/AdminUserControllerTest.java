package com.example.mediplan.admin;

import com.example.mediplan.user.Administrator;
import com.example.mediplan.user.Medecin;
import com.example.mediplan.user.Role;
import com.example.mediplan.user.User;
import com.example.mediplan.user.UserRepository;
import com.example.mediplan.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AdminUserControllerTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.13");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListUsersWithFilters() throws Exception {
        Administrator admin = Administrator.builder()
                .fullName("Admin Test")
                .email("admin@test.com")
                .role(Role.ADMIN)
                .provider("LOCAL")
                .emailVerified(true)
                .build();
        userRepository.save(admin);

        mockMvc.perform(get("/api/admin/users")
                        .param("q", "admin")
                        .param("size", "5")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("admin@test.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreatePatient() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("fullName", "Claire Martin");
        payload.put("email", "Claire.MARTIN@example.com");
        payload.put("password", "MotDePasse123!");
        payload.put("role", "PATIENT");

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("claire.martin@example.com"))
                .andExpect(jsonPath("$.role").value("PATIENT"));

        assertThat(userRepository.findByEmailIgnoreCase("claire.martin@example.com")).isPresent();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldChangeRoleToMedecin() throws Exception {
        Administrator admin = Administrator.builder()
                .fullName("Jean Dupont")
                .email("jean.dupont@example.com")
                .role(Role.ADMIN)
                .provider("LOCAL")
                .emailVerified(true)
                .licenseNumber("LIC-2000")
                .specialty("Pédiatrie")
                .build();
        User saved = userRepository.save(admin);

        Map<String, Object> payload = Map.of("role", "MEDECIN");

        mockMvc.perform(post("/api/admin/users/" + saved.getId() + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MEDECIN"))
                .andExpect(jsonPath("$.licenseNumber").value("LIC-2000"));

        User updated = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getRole()).isEqualTo(Role.MEDECIN);
        assertThat(updated).isInstanceOf(Medecin.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeactivateAndReactivateUser() throws Exception {
        Administrator admin = Administrator.builder()
                .fullName("Test Deactivation")
                .email("deactivate@example.com")
                .role(Role.ADMIN)
                .provider("LOCAL")
                .emailVerified(true)
                .build();
        User saved = userRepository.save(admin);

        mockMvc.perform(post("/api/admin/users/" + saved.getId() + "/deactivate"))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(saved.getId()).orElseThrow().isActive()).isFalse();

        mockMvc.perform(post("/api/admin/users/" + saved.getId() + "/reactivate"))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(saved.getId()).orElseThrow().isActive()).isTrue();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldResetPassword() throws Exception {
        Administrator admin = Administrator.builder()
                .fullName("Reset Test")
                .email("reset@example.com")
                .role(Role.ADMIN)
                .provider("LOCAL")
                .emailVerified(true)
                .passwordHash(userService.hashPassword("Ancien123!"))
                .build();
        User saved = userRepository.save(admin);

        Map<String, Object> payload = Map.of("newPassword", "Nouveau123!");

        mockMvc.perform(post("/api/admin/users/" + saved.getId() + "/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNoContent());

        User updated = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(userService.checkPassword("Nouveau123!", updated.getPasswordHash())).isTrue();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldExportCsv() throws Exception {
        Administrator admin = Administrator.builder()
                .fullName("Export Test")
                .email("export@example.com")
                .role(Role.ADMIN)
                .provider("LOCAL")
                .emailVerified(true)
                .build();
        userRepository.save(admin);

        mockMvc.perform(post("/api/admin/users/export")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id,fullName,email")));
    }
}
