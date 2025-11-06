package com.example.mediplan.user;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedecinRepository extends MongoRepository<Medecin, String> {
    boolean existsByLicenseNumber(String licenseNumber);
    Optional<Medecin> findByLicenseNumber(String licenseNumber); // add this
}
