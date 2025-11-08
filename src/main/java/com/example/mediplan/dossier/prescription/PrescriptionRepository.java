package com.example.mediplan.dossier.prescription;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescriptionRepository extends MongoRepository<Prescription, String> {

    Page<Prescription> findByPatientIdOrderByCreatedAtDesc(String patientId, Pageable pageable);

    Page<Prescription> findByMedecinIdOrderByCreatedAtDesc(String medecinId, Pageable pageable);

    boolean existsByConsultationId(String consultationId);
}
