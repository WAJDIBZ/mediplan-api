package com.example.mediplan.admin;

import com.example.mediplan.admin.dto.AdminAddressDTO;
import com.example.mediplan.admin.dto.AdminAddressInput;
import com.example.mediplan.admin.dto.AdminEmergencyContactDTO;
import com.example.mediplan.admin.dto.AdminEmergencyContactInput;
import com.example.mediplan.admin.dto.AdminUserDetailsDTO;
import com.example.mediplan.admin.dto.AdminUserListItemDTO;
import com.example.mediplan.user.Address;
import com.example.mediplan.user.EmergencyContact;
import com.example.mediplan.user.Medecin;
import com.example.mediplan.user.Patient;
import com.example.mediplan.user.User;
import org.springframework.stereotype.Component;

@Component
public class AdminUserMapper {

    /* ------------ List item (safe: base fields only) ------------ */
    public AdminUserListItemDTO toListItem(User user) {
        return AdminUserListItemDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .provider(user.getProvider())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /* ------------ Details (safe: base + subtype fields) ------------ */
    public AdminUserDetailsDTO toDetails(User user) {
        AdminUserDetailsDTO.AdminUserDetailsDTOBuilder b = AdminUserDetailsDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())               // if you keep “active” on User; else remove
                .emailVerified(user.isEmailVerified())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .address(toAddressDTO(user.getAddress()));

        if (user instanceof Patient p) {
            b.dateOfBirth(p.getDateOfBirth())
                    .gender(p.getGender())
                    .insuranceNumber(p.getInsuranceNumber())
                    .emergencyContact(toEmergencyContactDTO(p.getEmergencyContact()));
        }

        if (user instanceof Medecin m) {
            b.specialty(m.getSpecialty())
                    .licenseNumber(m.getLicenseNumber())
                    .yearsOfExperience(m.getYearsOfExperience())
                    .clinicName(m.getClinicName())
                    .clinicAddress(toAddressDTO(m.getClinicAddress()));
        }

        // If you have an Administrator subclass and no extra fields, nothing else to add.
        return b.build();
    }


    /* ------------ Inputs -> domain ------------ */
    public Address toAddress(AdminAddressInput input) {
        if (input == null) return null;
        return Address.builder()
                .line1(input.getLine1())
                .line2(input.getLine2())
                .city(input.getCity())
                .country(input.getCountry())
                .zip(input.getZip())
                .build();
    }

    public EmergencyContact toEmergencyContact(AdminEmergencyContactInput input) {
        if (input == null) return null;
        return EmergencyContact.builder()
                .name(input.getName())
                .phone(input.getPhone())
                .relation(input.getRelation())
                .build();
    }

    /* ------------ domain -> DTO ------------ */
    public AdminAddressDTO toAddressDTO(Address address) {
        if (address == null) return null;
        return AdminAddressDTO.builder()
                .line1(address.getLine1())
                .line2(address.getLine2())
                .city(address.getCity())
                .country(address.getCountry())
                .zip(address.getZip())
                .build();
    }

    public AdminEmergencyContactDTO toEmergencyContactDTO(EmergencyContact contact) {
        if (contact == null) return null;
        return AdminEmergencyContactDTO.builder()
                .name(contact.getName())
                .phone(contact.getPhone())
                .relation(contact.getRelation())
                .build();
    }
}
