package com.example.mediplan.auth;

import com.example.mediplan.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthControllerTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.13");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerPatientShouldReturnTokens() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("fullName", "Claire Martin");
        payload.put("email", "claire.martin@example.com");
        payload.put("password", "motdepasseFort123");
        payload.put("dateOfBirth", "1990-05-14");
        payload.put("gender", "FEMALE");

        mockMvc.perform(post("/api/auth/register/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void registerDoctorShouldReturnTokens() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("fullName", "Dr Antoine Leroy");
        payload.put("email", "antoine.leroy@example.com");
        payload.put("password", "MotDePasseSolide1");
        payload.put("specialty", "Cardiologie");
        payload.put("licenseNumber", "LIC-111111");
        payload.put("yearsOfExperience", 10);

        mockMvc.perform(post("/api/auth/register/doctor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void duplicateEmailShouldReturnConflict() throws Exception {
        registerPatient("claire.martin@example.com");

        Map<String, Object> doctor = new HashMap<>();
        doctor.put("fullName", "Dr Antoine Leroy");
        doctor.put("email", "claire.martin@example.com");
        doctor.put("password", "MotDePasseSolide1");
        doctor.put("specialty", "Cardiologie");
        doctor.put("licenseNumber", "LIC-222222");
        doctor.put("yearsOfExperience", 5);

        mockMvc.perform(post("/api/auth/register/doctor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doctor)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cet email est déjà utilisé."));
    }

    @Test
    void duplicateLicenseNumberShouldReturnConflict() throws Exception {
        Map<String, Object> doctor = new HashMap<>();
        doctor.put("fullName", "Dr Antoine Leroy");
        doctor.put("email", "antoine.leroy@example.com");
        doctor.put("password", "MotDePasseSolide1");
        doctor.put("specialty", "Cardiologie");
        doctor.put("licenseNumber", "LIC-333333");
        doctor.put("yearsOfExperience", 8);

        mockMvc.perform(post("/api/auth/register/doctor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doctor)))
                .andExpect(status().isOk());

        Map<String, Object> duplicate = new HashMap<>();
        duplicate.put("fullName", "Dr Brigitte Noel");
        duplicate.put("email", "brigitte.noel@example.com");
        duplicate.put("password", "MotDePasseSolide1");
        duplicate.put("specialty", "Dermatologie");
        duplicate.put("licenseNumber", "LIC-333333");
        duplicate.put("yearsOfExperience", 4);

        mockMvc.perform(post("/api/auth/register/doctor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ce numéro de licence est déjà enregistré."));
    }

    private void registerPatient(String email) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("fullName", "Claire Martin");
        payload.put("email", email);
        payload.put("password", "motdepasseFort123");
        payload.put("dateOfBirth", "1990-05-14");
        payload.put("gender", "FEMALE");

        mockMvc.perform(post("/api/auth/register/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());
    }
}
