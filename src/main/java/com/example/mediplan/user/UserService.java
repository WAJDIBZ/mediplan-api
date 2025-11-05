package com.example.mediplan.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Manual constructor
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(String fullName, String email, String rawPassword, User.Role role) {
        if (userRepository.existsByEmail(email.toLowerCase())) {
            throw new IllegalArgumentException("Email already used");
        }
        User user = new User(
                null,
                fullName,
                email.toLowerCase(),
                passwordEncoder.encode(rawPassword),
                role == null ? User.Role.PATIENT : role,
                false
        );
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