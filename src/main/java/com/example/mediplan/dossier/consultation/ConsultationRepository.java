package com.example.mediplan.dossier.consultation;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultationRepository extends MongoRepository<Consultation, String> {

    Page<Consultation> findByPatientIdOrderByDateDesc(String patientId, Pageable pageable);

    Page<Consultation> findByMedecinIdOrderByDateDesc(String medecinId, Pageable pageable);

    List<Consultation> findByRendezVousId(String rendezVousId);
}
