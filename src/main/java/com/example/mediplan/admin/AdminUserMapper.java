package com.example.mediplan.admin;

import com.example.mediplan.admin.dto.AdminAddressDTO;
import com.example.mediplan.admin.dto.AdminAddressInput;
import com.example.mediplan.admin.dto.AdminEmergencyContactDTO;
import com.example.mediplan.admin.dto.AdminEmergencyContactInput;
import com.example.mediplan.admin.dto.AdminUserDetailsDTO;
import com.example.mediplan.admin.dto.AdminUserListItemDTO;
import com.example.mediplan.user.Address;
import com.example.mediplan.user.EmergencyContact;
import com.example.mediplan.user.User;
import org.springframework.stereotype.Component;

@Component
public class AdminUserMapper {

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

    public AdminUserDetailsDTO toDetails(User user) {
        return AdminUserDetailsDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .emailVerified(user.isEmailVerified())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .insuranceNumber(user.getInsuranceNumber())
                .emergencyContact(toEmergencyContactDTO(user.getEmergencyContact()))
                .address(toAddressDTO(user.getAddress()))
                .specialty(user.getSpecialty())
                .licenseNumber(user.getLicenseNumber())
                .yearsOfExperience(user.getYearsOfExperience())
                .clinicName(user.getClinicName())
                .clinicAddress(toAddressDTO(user.getClinicAddress()))
                .build();
    }

    public Address toAddress(AdminAddressInput input) {
        if (input == null) {
            return null;
        }
        return Address.builder()
                .line1(input.getLine1())
                .line2(input.getLine2())
                .city(input.getCity())
                .country(input.getCountry())
                .zip(input.getZip())
                .build();
    }

    public EmergencyContact toEmergencyContact(AdminEmergencyContactInput input) {
        if (input == null) {
            return null;
        }
        return EmergencyContact.builder()
                .name(input.getName())
                .phone(input.getPhone())
                .relation(input.getRelation())
                .build();
    }

    public AdminAddressDTO toAddressDTO(Address address) {
        if (address == null) {
            return null;
        }
        return AdminAddressDTO.builder()
                .line1(address.getLine1())
                .line2(address.getLine2())
                .city(address.getCity())
                .country(address.getCountry())
                .zip(address.getZip())
                .build();
    }

    public AdminEmergencyContactDTO toEmergencyContactDTO(EmergencyContact contact) {
        if (contact == null) {
            return null;
        }
        return AdminEmergencyContactDTO.builder()
                .name(contact.getName())
                .phone(contact.getPhone())
                .relation(contact.getRelation())
                .build();
    }
}
