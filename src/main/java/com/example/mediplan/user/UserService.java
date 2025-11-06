package com.example.mediplan.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final MedecinRepository medecinRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       MedecinRepository medecinRepository,
                       BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.medecinRepository = medecinRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean emailExists(String email) {
        return email != null && userRepository.existsByEmail(email);
    }

    public boolean licenseExists(String licenseNumber) {
        return licenseNumber != null && medecinRepository.existsByLicenseNumber(licenseNumber);
    }

    public <T extends User> T save(T user) {
        return userRepository.save(user);
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    public boolean checkPassword(String raw, String hash) {
        return hash != null && passwordEncoder.matches(raw, hash);
    }

    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public Optional<User> findByProviderAndProviderId(String provider, String providerId) {
        if (provider == null || providerId == null) {
            return Optional.empty();
        }
        return userRepository.findByProviderAndProviderId(provider, providerId);
    }

    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
