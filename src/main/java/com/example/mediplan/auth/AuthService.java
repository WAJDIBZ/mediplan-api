package com.example.mediplan.auth;

import com.example.mediplan.auth.dto.AuthResponse;
import com.example.mediplan.auth.dto.DoctorRegisterRequest;
import com.example.mediplan.auth.dto.LoginRequest;
import com.example.mediplan.auth.dto.PatientRegisterRequest;
import com.example.mediplan.common.exception.InvalidCredentialsException;
import com.example.mediplan.common.exception.ResourceConflictException;
import com.example.mediplan.security.jwt.JwtService;
import com.example.mediplan.user.Medecin;
import com.example.mediplan.user.Patient;
import com.example.mediplan.user.User;
import com.example.mediplan.user.UserMapper;
import com.example.mediplan.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Service
public class AuthService {

    private final UserService userService;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    public AuthService(UserService userService, UserMapper userMapper, JwtService jwtService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
    }

    public AuthResponse registerPatient(PatientRegisterRequest request) {
        sanitizePatientRequest(request);
        String normalizedEmail = normalizeEmail(request.getEmail());
        ensureEmailAvailable(normalizedEmail);
        request.setEmail(normalizedEmail);

        String hashedPassword = userService.hashPassword(request.getPassword());
        Patient patient = userMapper.toPatient(request, hashedPassword, normalizedEmail);
        Patient saved = userService.save(patient);
        return generateTokens(saved);
    }

    public AuthResponse registerDoctor(DoctorRegisterRequest request) {
        sanitizeDoctorRequest(request);
        String normalizedEmail = normalizeEmail(request.getEmail());
        ensureEmailAvailable(normalizedEmail);
        ensureLicenseAvailable(request.getLicenseNumber());
        request.setEmail(normalizedEmail);

        String hashedPassword = userService.hashPassword(request.getPassword());
        Medecin medecin = userMapper.toMedecin(request, hashedPassword, normalizedEmail);
        Medecin saved = userService.save(medecin);
        return generateTokens(saved);
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        String password = trim(request.getPassword());

        User user = userService.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Identifiants invalides."));
        if (!userService.checkPassword(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Identifiants invalides.");
        }
        return generateTokens(user);
    }

    public AuthResponse refresh(String refreshToken) {
        var claims = jwtService.parseRefreshToken(refreshToken).getBody();
        String userId = claims.getSubject();
        User user = userService.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("Utilisateur introuvable."));
        return generateTokens(user);
    }

    private AuthResponse generateTokens(User user) {
        String access = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refresh = jwtService.generateRefreshToken(user.getId());
        return new AuthResponse(access, refresh, user.getRole().name()); // ✅ send role directly
    }


    private void ensureEmailAvailable(String email) {
        if (userService.emailExists(email)) {
            throw new ResourceConflictException("Cet email est déjà utilisé.");
        }
    }

    private void ensureLicenseAvailable(String licenseNumber) {
        if (licenseNumber != null && userService.licenseExists(licenseNumber)) {
            throw new ResourceConflictException("Ce numéro de licence est déjà enregistré.");
        }
    }

    private String normalizeEmail(String email) {
        String trimmed = trim(email);
        return trimmed == null ? null : trimmed.toLowerCase(Locale.ROOT);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String trimToNull(String value) {
        String trimmed = trim(value);
        return StringUtils.hasText(trimmed) ? trimmed : null;
    }

    private void sanitizePatientRequest(PatientRegisterRequest request) {
        request.setFullName(trim(request.getFullName()));
        request.setEmail(trim(request.getEmail()));
        request.setPassword(trim(request.getPassword()));
        request.setPhone(trimToNull(request.getPhone()));
        request.setInsuranceNumber(trimToNull(request.getInsuranceNumber()));
        if (request.getAddress() != null) {
            request.getAddress().setLine1(trim(request.getAddress().getLine1()));
            request.getAddress().setLine2(trimToNull(request.getAddress().getLine2()));
            request.getAddress().setCity(trim(request.getAddress().getCity()));
            request.getAddress().setCountry(trim(request.getAddress().getCountry()));
            request.getAddress().setZip(trim(request.getAddress().getZip()));
        }
        if (request.getEmergencyContact() != null) {
            request.getEmergencyContact().setName(trim(request.getEmergencyContact().getName()));
            request.getEmergencyContact().setPhone(trim(request.getEmergencyContact().getPhone()));
            request.getEmergencyContact().setRelation(trim(request.getEmergencyContact().getRelation()));
        }
    }

    private void sanitizeDoctorRequest(DoctorRegisterRequest request) {
        request.setFullName(trim(request.getFullName()));
        request.setEmail(trim(request.getEmail()));
        request.setPassword(trim(request.getPassword()));
        request.setPhone(trimToNull(request.getPhone()));
        request.setSpecialty(trim(request.getSpecialty()));
        request.setLicenseNumber(trim(request.getLicenseNumber()));
        request.setClinicName(trimToNull(request.getClinicName()));
        request.setAvatarUrl(trimToNull(request.getAvatarUrl()));
        if (request.getClinicAddress() != null) {
            request.getClinicAddress().setLine1(trim(request.getClinicAddress().getLine1()));
            request.getClinicAddress().setLine2(trimToNull(request.getClinicAddress().getLine2()));
            request.getClinicAddress().setCity(trim(request.getClinicAddress().getCity()));
            request.getClinicAddress().setCountry(trim(request.getClinicAddress().getCountry()));
            request.getClinicAddress().setZip(trim(request.getClinicAddress().getZip()));
        }
    }
}
