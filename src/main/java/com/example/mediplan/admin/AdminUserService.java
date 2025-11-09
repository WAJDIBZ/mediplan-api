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
import com.example.mediplan.user.*;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminUserService.class);

    private final UserRepository userRepository;
    private final UserService userService;
    private final AdminUserMapper mapper;

    /* ========== Queries & Reads ========== */

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

    /* ========== Create / Update / Delete ========== */

    public AdminUserDetailsDTO createUser(AdminCreateUserRequest request) {
        sanitizeCreateRequest(request);

        String email = requireNormalizedEmail(request.getEmail());
        ensureEmailAvailable(email, null);

        Role role = request.getRole();
        String licenseNumber = trimToNull(request.getLicenseNumber());
        String specialty = trimToNull(request.getSpecialty());

        if (role == Role.MEDECIN) {
            if (!StringUtils.hasText(licenseNumber)) {
                throw new BusinessRuleException("Le numéro de licence est requis pour un médecin.");
            }
            if (!StringUtils.hasText(specialty)) {
                throw new BusinessRuleException("La spécialité est obligatoire pour un médecin.");
            }
            ensureLicenseAvailable(licenseNumber, null);
        }

        User user = buildUserForCreation(request, email, licenseNumber, specialty);
        User saved = userRepository.save(user);
        return mapper.toDetails(saved);
    }

    public AdminUserDetailsDTO updateUser(String id, AdminUpdateUserRequest request) {
        sanitizeUpdateRequest(request);
        User user = getById(id);

        // ---- Common fields (base User)
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

        // ---- Patient-only fields
        if (request.getInsuranceNumber() != null || request.getEmergencyContact() != null) {
            if (!(user instanceof Patient p)) {
                throw new BusinessRuleException("Champs patient uniquement (insuranceNumber, emergencyContact).");
            }
            if (request.getInsuranceNumber() != null) {
                p.setInsuranceNumber(trimToNull(request.getInsuranceNumber()));
            }
            if (request.getEmergencyContact() != null) {
                p.setEmergencyContact(mapper.toEmergencyContact(request.getEmergencyContact()));
            }
        }

        // ---- Medecin-only fields
        boolean wantsDoctorFields =
                request.getSpecialty() != null ||
                        request.getLicenseNumber() != null ||
                        request.getYearsOfExperience() != null ||
                        request.getClinicName() != null ||
                        request.getClinicAddress() != null;

        if (wantsDoctorFields) {
            if (!(user instanceof Medecin m)) {
                throw new BusinessRuleException("Champs médecin uniquement (specialty, licenseNumber, yearsOfExperience, clinic...).");
            }
            if (request.getSpecialty() != null) {
                String specialty = trimToNull(request.getSpecialty());
                if (!StringUtils.hasText(specialty)) {
                    throw new BusinessRuleException("La spécialité est obligatoire pour un médecin.");
                }
                m.setSpecialty(specialty);
            }
            if (request.getLicenseNumber() != null) {
                String newLicense = trimToNull(request.getLicenseNumber());
                if (!StringUtils.hasText(newLicense)) {
                    throw new BusinessRuleException("Le numéro de licence est requis pour un médecin.");
                }
                if (!Objects.equals(newLicense, m.getLicenseNumber())) {
                    ensureLicenseAvailable(newLicense, user.getId());
                    m.setLicenseNumber(newLicense);
                }
            }
            if (request.getYearsOfExperience() != null) {
                m.setYearsOfExperience(request.getYearsOfExperience());
            }
            if (request.getClinicName() != null) {
                m.setClinicName(trimToNull(request.getClinicName()));
            }
            if (request.getClinicAddress() != null) {
                m.setClinicAddress(mapper.toAddress(request.getClinicAddress()));
            }
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
        sanitizeChangeRoleRequest(request);
        if (request.getRole() == null) {
            throw new BusinessRuleException("Le rôle cible est obligatoire.");
        }
        User current = getById(id);
        Role targetRole = request.getRole();
        if (current.getRole() == targetRole) {
            return mapper.toDetails(current);
        }

        User converted = switch (targetRole) {
            case ADMIN -> convertToAdministrator(current);
            case PATIENT -> convertToPatient(current);
            case MEDECIN -> {
                // pull doctor fields from the request; they are required when promoting to MEDECIN
                String specialty = trimToNull(request.getSpecialty());
                String license   = trimToNull(request.getLicenseNumber());
                Integer yoe      = request.getYearsOfExperience();
                String clinic    = trimToNull(request.getClinicName());
                var clinicAddr   = mapper.toAddress(request.getClinicAddress());

                if (!org.springframework.util.StringUtils.hasText(specialty)) {
                    throw new BusinessRuleException("La spécialité est obligatoire pour promouvoir en médecin.");
                }
                if (!org.springframework.util.StringUtils.hasText(license)) {
                    throw new BusinessRuleException("Le numéro de licence est obligatoire pour promouvoir en médecin.");
                }
                ensureLicenseAvailable(license, current.getId());

                yield convertToMedecin(current, specialty, license, yoe, clinic, clinicAddr);
            }
        };

        converted.setRole(targetRole);
        User saved = userRepository.save(converted);
        LOGGER.info("Changement de rôle pour l'utilisateur {} : {} -> {}", id, current.getRole(), targetRole);
        return mapper.toDetails(saved);
    }


    /* ========== Bulk Ops ========== */

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

    /* ========== Export ========== */

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
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r")) {
            return '"' + escaped + '"';
        }
        return escaped;
    }

    /* ========== Builders & Converters ========== */

    private User buildUserForCreation(AdminCreateUserRequest request, String email, String licenseNumber, String specialty) {
        String passwordHash = request.getPassword() != null ? userService.hashPassword(request.getPassword()) : null;
        String phone = trimToNull(request.getPhone());
        String avatarUrl = trimToNull(request.getAvatarUrl());

        switch (request.getRole()) {
            case PATIENT:
                return Patient.builder()
                        .fullName(request.getFullName())
                        .email(email)
                        .passwordHash(passwordHash)
                        .role(Role.PATIENT)
                        .emailVerified(false)
                        .provider("LOCAL")
                        .active(true)
                        .phone(phone)
                        .avatarUrl(avatarUrl)
                        .address(mapper.toAddress(request.getAddress()))
                        .insuranceNumber(trimToNull(request.getInsuranceNumber()))
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
                        .active(true)
                        .phone(phone)
                        .avatarUrl(avatarUrl)
                        .specialty(specialty)
                        .licenseNumber(licenseNumber)
                        .yearsOfExperience(request.getYearsOfExperience())
                        .clinicName(trimToNull(request.getClinicName()))
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
                        .active(true)
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
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .build();

        if (user instanceof Patient p) {
            // copy patient extras if present
            // (only if you really want admins to carry over these fields)
            // e.g. store them somewhere else or just ignore
            // admin.setWhatever(...);  // usually admins don’t have these
        }
        if (user instanceof Medecin m) {
            // same comment as above
        }

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
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .build();

        // If the source user already is a Patient, copy its patient fields:
        if (user instanceof Patient p0) {
            patient.setDateOfBirth(p0.getDateOfBirth());
            patient.setGender(p0.getGender());
            patient.setInsuranceNumber(p0.getInsuranceNumber());
            patient.setEmergencyContact(p0.getEmergencyContact());
        }

        patient.setCreatedAt(user.getCreatedAt());
        patient.setUpdatedAt(user.getUpdatedAt());
        return patient;
    }


    private User convertToMedecin(
            User user,
            String specialty,
            String licenseNumber,
            Integer yearsOfExperience,
            String clinicName,
            Address clinicAddress
    ) {
        String sp = trimToNull(specialty);
        String lic = trimToNull(licenseNumber);
        if (!StringUtils.hasText(lic)) throw new BusinessRuleException("Licence requise.");
        if (!StringUtils.hasText(sp))  throw new BusinessRuleException("Spécialité requise.");

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
                .specialty(sp)
                .licenseNumber(lic)
                .yearsOfExperience(yearsOfExperience)
                .clinicName(trimToNull(clinicName))
                .clinicAddress(clinicAddress)
                .build();

        medecin.setCreatedAt(user.getCreatedAt());
        medecin.setUpdatedAt(user.getUpdatedAt());
        return medecin;
    }


    /* ========== Guards & Sanitizers ========== */

    private void ensureEmailAvailable(String email, String currentId) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(existing -> {
            if (!Objects.equals(existing.getId(), currentId)) {
                throw new ResourceConflictException("Cet email est déjà utilisé.");
            }
        });
    }

    private void ensureLicenseAvailable(String licenseNumber, String currentUserId) {
        if (!StringUtils.hasText(licenseNumber)) {
            return;
        }
        userService.findByLicenseNumber(licenseNumber).ifPresent(existing -> {
            if (!Objects.equals(existing.getId(), currentUserId)) {
                throw new ResourceConflictException("Ce numéro de licence est déjà enregistré.");
            }
        });
    }

    private User getById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
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

    private void sanitizeChangeRoleRequest(AdminChangeRoleRequest request) {
        if (request == null) {
            return;
        }
        request.setSpecialty(trimToNull(request.getSpecialty()));
        request.setLicenseNumber(trimToNull(request.getLicenseNumber()));
        request.setClinicName(trimToNull(request.getClinicName()));
        sanitizeAddress(request.getClinicAddress());
    }

    private void sanitizeAddress(com.example.mediplan.admin.dto.AdminAddressInput address) {
        if (address == null) return;
        address.setLine1(trim(address.getLine1()));
        address.setLine2(trimToNull(address.getLine2()));
        address.setCity(trim(address.getCity()));
        address.setCountry(trim(address.getCountry()));
        address.setZip(trim(address.getZip()));
    }

    private void sanitizeEmergencyContact(com.example.mediplan.admin.dto.AdminEmergencyContactInput contact) {
        if (contact == null) return;
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
        String t = trim(value);
        return StringUtils.hasText(t) ? t : null;
    }
}
