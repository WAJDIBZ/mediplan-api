package com.example.mediplan.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    public User createUser(String fullName, String email, String rawPassword, User.Role role) {
        if (userRepository.existsByEmail(email.toLowerCase())) {
            throw new IllegalArgumentException("Email already used");
        }
        User user = User.builder()
                .fullName(fullName)
                .email(email.toLowerCase())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(role == null ? User.Role.PATIENT : role)
                .emailVerified(false)
                .build();
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public boolean checkPassword(String raw, String hash) {
        return passwordEncoder.matches(raw, hash);
    }
}
