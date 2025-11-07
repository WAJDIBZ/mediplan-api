package com.example.mediplan.user;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String>, UserRepositoryCustom {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
    Optional<User> findByLicenseNumber(String licenseNumber);
}
