package com.example.mediplan.admin;

import com.example.mediplan.admin.dto.AdminBulkActionRequest;
import com.example.mediplan.admin.dto.AdminChangeRoleRequest;
import com.example.mediplan.admin.dto.AdminCreateUserRequest;
import com.example.mediplan.admin.dto.AdminResetPasswordRequest;
import com.example.mediplan.admin.dto.AdminUpdateUserRequest;
import com.example.mediplan.admin.dto.AdminUserDetailsDTO;
import com.example.mediplan.admin.dto.AdminUserListItemDTO;
import com.example.mediplan.common.exception.BusinessRuleException;
import com.example.mediplan.common.exception.ResourceConflictException;
import com.example.mediplan.common.exception.ResourceNotFoundException;
import com.example.mediplan.user.AdminUserFilter;
import com.example.mediplan.user.Administrator;
import com.example.mediplan.user.Medecin;
import com.example.mediplan.user.Patient;
import com.example.mediplan.user.Role;
import com.example.mediplan.user.User;
import com.example.mediplan.user.UserRepository;
import com.example.mediplan.user.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminUserService.class);

    private final UserRepository userRepository;
    private final UserService userService;
    private final AdminUserMapper mapper;

    public Page<AdminUserListItemDTO> listUsers(String q, Role role, Boolean active, String provider, Pageable pageable) {
        String normalizedProvider = trimToNull(provider);
        AdminUserFilter filter = AdminUserFilter.builder()
                .query(trimToNull(q))
                .role(role)
                .active(active)
                .provider(normalizedProvider != null ? normalizedProvider.toUpperCase(Locale.ROOT) : null)
                .build();
        return userRepository.search(filter, pageable).map(mapper::toListItem);
    }

    public AdminUserDetailsDTO getUser(String id) {
        return mapper.toDetails(getById(id));
    }

    public AdminUserDetailsDTO createUser(AdminCreateUserRequest request) {
        sanitizeCreateRequest(request);
        String email = requireNormalizedEmail(request.getEmail());
        ensureEmailAvailable(email, null);

        Role role = request.getRole();
        String licenseNumber = trimToNull(request.getLicenseNumber());
        if (role == Role.MEDECIN) {
            if (!StringUtils.hasText(licenseNumber)) {
                throw new BusinessRuleException("Le numéro de licence est requis pour un médecin.");
            }
            if (!StringUtils.hasText(request.getSpecialty())) {
                throw new BusinessRuleException("La spécialité est obligatoire pour un médecin.");
            }
            ensureLicenseAvailable(licenseNumber, null);
        }

        User user = buildUserForCreation(request, email, licenseNumber);
        User saved = userRepository.save(user);
        return mapper.toDetails(saved);
    }

    public AdminUserDetailsDTO updateUser(String id, AdminUpdateUserRequest request) {
        sanitizeUpdateRequest(request);
        User user = getById(id);

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            String normalizedEmail = requireNormalizedEmail(request.getEmail());
            ensureEmailAvailable(normalizedEmail, user.getId());
            user.setEmail(normalizedEmail);
        }
        if (request.getPassword() != null) {
            user.setPasswordHash(userService.hashPassword(request.getPassword()));
        }
        if (request.getPhone() != null) {
            user.setPhone(trimToNull(request.getPhone()));
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(trimToNull(request.getAvatarUrl()));
        }
        if (request.getAddress() != null) {
            user.setAddress(mapper.toAddress(request.getAddress()));
        }
        if (request.getInsuranceNumber() != null) {
            user.setInsuranceNumber(trimToNull(request.getInsuranceNumber()));
        }
        if (request.getEmergencyContact() != null) {
            user.setEmergencyContact(mapper.toEmergencyContact(request.getEmergencyContact()));
        }
        if (request.getSpecialty() != null || request.getLicenseNumber() != null || request.getYearsOfExperience() != null
                || request.getClinicName() != null || request.getClinicAddress() != null) {
            ensureRoleIsMedecin(user);
        }
        if (request.getSpecialty() != null) {
            String specialty = trimToNull(request.getSpecialty());
            if (!StringUtils.hasText(specialty)) {
                throw new BusinessRuleException("La spécialité est obligatoire pour un médecin.");
            }
            user.setSpecialty(specialty);
        }
        if (request.getLicenseNumber() != null) {
            String licenseNumber = trimToNull(request.getLicenseNumber());
            if (!StringUtils.hasText(licenseNumber)) {
                throw new BusinessRuleException("Le numéro de licence est requis pour un médecin.");
            }
            ensureLicenseAvailable(licenseNumber, user.getId());
            user.setLicenseNumber(licenseNumber);
        }
        if (request.getYearsOfExperience() != null) {
            user.setYearsOfExperience(request.getYearsOfExperience());
        }
        if (request.getClinicName() != null) {
            user.setClinicName(trimToNull(request.getClinicName()));
        }
        if (request.getClinicAddress() != null) {
            user.setClinicAddress(mapper.toAddress(request.getClinicAddress()));
        }

        User saved = userRepository.save(user);
        return mapper.toDetails(saved);
    }

    public void deactivateUser(String id) {
        User user = getById(id);
        if (user.isActive()) {
            user.setActive(false);
            userRepository.save(user);
        }
    }

    public void reactivateUser(String id) {
        User user = getById(id);
        if (!user.isActive()) {
            user.setActive(true);
            userRepository.save(user);
        }
    }

    public void deleteUser(String id, boolean hardDelete) {
        if (hardDelete) {
            if (!userRepository.existsById(id)) {
                throw new ResourceNotFoundException("Utilisateur introuvable");
            }
            userRepository.deleteById(id);
            return;
        }
        deactivateUser(id);
    }

    public void resetPassword(String id, AdminResetPasswordRequest request) {
        User user = getById(id);
        user.setPasswordHash(userService.hashPassword(request.getNewPassword()));
        userRepository.save(user);
    }

    public AdminUserDetailsDTO changeRole(String id, AdminChangeRoleRequest request) {
        User current = getById(id);
        Role targetRole = request.getRole();
        if (current.getRole() == targetRole) {
            return mapper.toDetails(current);
        }
        User converted = switch (targetRole) {
            case ADMIN -> convertToAdministrator(current);
            case PATIENT -> convertToPatient(current);
            case MEDECIN -> convertToMedecin(current);
        };
        converted.setRole(targetRole);
        User saved = userRepository.save(converted);
        LOGGER.info("Changement de rôle pour l'utilisateur {} : {} -> {}", id, current.getRole(), targetRole);
        return mapper.toDetails(saved);
    }

    public void bulkDeactivate(AdminBulkActionRequest request) {
        for (String id : request.getIds()) {
            try {
                deactivateUser(id);
            } catch (ResourceNotFoundException ignored) {
            }
        }
    }

    public void bulkReactivate(AdminBulkActionRequest request) {
        for (String id : request.getIds()) {
            try {
                reactivateUser(id);
            } catch (ResourceNotFoundException ignored) {
            }
        }
    }

    public void bulkDelete(AdminBulkActionRequest request) {
        for (String id : request.getIds()) {
            try {
                deleteUser(id, true);
            } catch (ResourceNotFoundException ignored) {
            }
        }
    }

    public byte[] exportCsv(String q, Role role, Boolean active, String provider, Sort sort) {
        String normalizedProvider = trimToNull(provider);
        AdminUserFilter filter = AdminUserFilter.builder()
                .query(trimToNull(q))
                .role(role)
                .active(active)
                .provider(normalizedProvider != null ? normalizedProvider.toUpperCase(Locale.ROOT) : null)
                .build();
        List<User> users = userRepository.findAll(filter, sort);
        String csv = buildCsv(users);
        return csv.getBytes(StandardCharsets.UTF_8);
    }

    private User buildUserForCreation(AdminCreateUserRequest request, String email, String licenseNumber) {
        String passwordHash = request.getPassword() != null ? userService.hashPassword(request.getPassword()) : null;
        String phone = trimToNull(request.getPhone());
        String avatarUrl = trimToNull(request.getAvatarUrl());
        String insuranceNumber = trimToNull(request.getInsuranceNumber());
        String specialty = trimToNull(request.getSpecialty());
        String clinicName = trimToNull(request.getClinicName());
        switch (request.getRole()) {
            case PATIENT:
                return Patient.builder()
                        .fullName(request.getFullName())
                        .email(email)
                        .passwordHash(passwordHash)
                        .role(Role.PATIENT)
                        .emailVerified(false)
                        .provider("LOCAL")
                        .phone(phone)
                        .avatarUrl(avatarUrl)
                        .address(mapper.toAddress(request.getAddress()))
                        .insuranceNumber(insuranceNumber)
                        .emergencyContact(mapper.toEmergencyContact(request.getEmergencyContact()))
                        .build();
            case MEDECIN:
                return Medecin.builder()
                        .fullName(request.getFullName())
                        .email(email)
                        .passwordHash(passwordHash)
                        .role(Role.MEDECIN)
                        .emailVerified(false)
                        .provider("LOCAL")
                        .phone(phone)
                        .avatarUrl(avatarUrl)
                        .specialty(specialty)
                        .licenseNumber(licenseNumber)
                        .yearsOfExperience(request.getYearsOfExperience())
                        .clinicName(clinicName)
                        .clinicAddress(mapper.toAddress(request.getClinicAddress()))
                        .build();
            case ADMIN:
                return Administrator.builder()
                        .fullName(request.getFullName())
                        .email(email)
                        .passwordHash(passwordHash)
                        .role(Role.ADMIN)
                        .emailVerified(false)
                        .provider("LOCAL")
                        .phone(phone)
                        .avatarUrl(avatarUrl)
                        .address(mapper.toAddress(request.getAddress()))
                        .build();
            default:
                throw new IllegalStateException("Rôle non pris en charge");
        }
    }

    private User convertToAdministrator(User user) {
        Administrator admin = Administrator.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .role(Role.ADMIN)
                .emailVerified(user.isEmailVerified())
                .active(user.isActive())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .address(user.getAddress())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .insuranceNumber(user.getInsuranceNumber())
                .emergencyContact(user.getEmergencyContact())
                .specialty(user.getSpecialty())
                .licenseNumber(user.getLicenseNumber())
                .yearsOfExperience(user.getYearsOfExperience())
                .clinicName(user.getClinicName())
                .clinicAddress(user.getClinicAddress())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .build();
        admin.setCreatedAt(user.getCreatedAt());
        admin.setUpdatedAt(user.getUpdatedAt());
        return admin;
    }

    private User convertToPatient(User user) {
        Patient patient = Patient.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .role(Role.PATIENT)
                .emailVerified(user.isEmailVerified())
                .active(user.isActive())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .address(user.getAddress())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .insuranceNumber(user.getInsuranceNumber())
                .emergencyContact(user.getEmergencyContact())
                .build();
        patient.setCreatedAt(user.getCreatedAt());
        patient.setUpdatedAt(user.getUpdatedAt());
        return patient;
    }

    private User convertToMedecin(User user) {
        String license = trimToNull(user.getLicenseNumber());
        if (!StringUtils.hasText(license)) {
            throw new BusinessRuleException("Impossible de promouvoir cet utilisateur : licence manquante.");
        }
        String specialty = trimToNull(user.getSpecialty());
        if (!StringUtils.hasText(specialty)) {
            throw new BusinessRuleException("Impossible de promouvoir cet utilisateur : spécialité manquante.");
        }
        ensureLicenseAvailable(license, user.getId());
        Medecin medecin = Medecin.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .role(Role.MEDECIN)
                .emailVerified(user.isEmailVerified())
                .active(user.isActive())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .specialty(specialty)
                .licenseNumber(license)
                .yearsOfExperience(user.getYearsOfExperience())
                .clinicName(user.getClinicName())
                .clinicAddress(user.getClinicAddress())
                .build();
        medecin.setCreatedAt(user.getCreatedAt());
        medecin.setUpdatedAt(user.getUpdatedAt());
        return medecin;
    }

    private void ensureEmailAvailable(String email, String currentId) {
        Optional<User> existing = userRepository.findByEmailIgnoreCase(email);
        if (existing.isPresent() && !Objects.equals(existing.get().getId(), currentId)) {
            throw new ResourceConflictException("Cet email est déjà utilisé.");
        }
    }

    private void ensureLicenseAvailable(String licenseNumber, String currentId) {
        if (!StringUtils.hasText(licenseNumber)) {
            return;
        }
        Optional<User> existing = userRepository.findByLicenseNumber(licenseNumber);
        if (existing.isPresent() && !Objects.equals(existing.get().getId(), currentId)) {
            throw new ResourceConflictException("Ce numéro de licence est déjà enregistré.");
        }
    }

    private User getById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    private void ensureRoleIsMedecin(User user) {
        if (user.getRole() != Role.MEDECIN) {
            throw new BusinessRuleException("Ces champs ne sont disponibles que pour les médecins.");
        }
    }

    private void sanitizeCreateRequest(AdminCreateUserRequest request) {
        request.setFullName(trim(request.getFullName()));
        request.setEmail(trim(request.getEmail()));
        request.setPassword(trimToNull(request.getPassword()));
        request.setPhone(trimToNull(request.getPhone()));
        request.setAvatarUrl(trimToNull(request.getAvatarUrl()));
        request.setInsuranceNumber(trimToNull(request.getInsuranceNumber()));
        request.setSpecialty(trimToNull(request.getSpecialty()));
        request.setLicenseNumber(trimToNull(request.getLicenseNumber()));
        request.setClinicName(trimToNull(request.getClinicName()));
        sanitizeAddress(request.getAddress());
        sanitizeAddress(request.getClinicAddress());
        sanitizeEmergencyContact(request.getEmergencyContact());
    }

    private void sanitizeUpdateRequest(AdminUpdateUserRequest request) {
        request.setFullName(trim(request.getFullName()));
        request.setEmail(trim(request.getEmail()));
        request.setPassword(trimToNull(request.getPassword()));
        request.setPhone(trimToNull(request.getPhone()));
        request.setAvatarUrl(trimToNull(request.getAvatarUrl()));
        request.setInsuranceNumber(trimToNull(request.getInsuranceNumber()));
        request.setSpecialty(trimToNull(request.getSpecialty()));
        request.setLicenseNumber(trimToNull(request.getLicenseNumber()));
        request.setClinicName(trimToNull(request.getClinicName()));
        sanitizeAddress(request.getAddress());
        sanitizeAddress(request.getClinicAddress());
        sanitizeEmergencyContact(request.getEmergencyContact());
    }

    private void sanitizeAddress(com.example.mediplan.admin.dto.AdminAddressInput address) {
        if (address == null) {
            return;
        }
        address.setLine1(trim(address.getLine1()));
        address.setLine2(trimToNull(address.getLine2()));
        address.setCity(trim(address.getCity()));
        address.setCountry(trim(address.getCountry()));
        address.setZip(trim(address.getZip()));
    }

    private void sanitizeEmergencyContact(com.example.mediplan.admin.dto.AdminEmergencyContactInput contact) {
        if (contact == null) {
            return;
        }
        contact.setName(trim(contact.getName()));
        contact.setPhone(trim(contact.getPhone()));
        contact.setRelation(trim(contact.getRelation()));
    }

    private String requireNormalizedEmail(String email) {
        String normalized = normalizeEmail(email);
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessRuleException("L'adresse email est invalide.");
        }
        return normalized;
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

    private String buildCsv(List<User> users) {
        String header = "id,fullName,email,role,active,provider,createdAt";
        List<String> rows = users.stream()
                .map(user -> String.join(",",
                        escapeCsvValue(user.getId()),
                        escapeCsvValue(user.getFullName()),
                        escapeCsvValue(user.getEmail()),
                        escapeCsvValue(user.getRole() != null ? user.getRole().name() : ""),
                        escapeCsvValue(String.valueOf(user.isActive())),
                        escapeCsvValue(user.getProvider()),
                        escapeCsvValue(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "")
                ))
                .collect(Collectors.toList());
        if (rows.isEmpty()) {
            return header;
        }
        return header + "\n" + String.join("\n", rows);
    }

    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r")) {
            return '"' + escaped + '"';
        }
        return escaped;
    }
}
