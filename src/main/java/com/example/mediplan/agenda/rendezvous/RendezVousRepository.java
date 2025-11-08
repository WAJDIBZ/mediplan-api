package com.example.mediplan.agenda.rendezvous;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RendezVousRepository extends MongoRepository<RendezVous, String> {

    boolean existsByMedecinIdAndStatutInAndDebutLessThanEqualAndFinGreaterThanEqual(
            String medecinId, List<RendezVousStatut> statuts, Instant fin, Instant debut);

    Page<RendezVous> findByMedecinIdAndDebutBetween(String medecinId, Instant from, Instant to, Pageable pageable);

    Page<RendezVous> findByPatientIdAndDebutBetween(String patientId, Instant from, Instant to, Pageable pageable);

    Page<RendezVous> findByDebutBetween(Instant from, Instant to, Pageable pageable);

    List<RendezVous> findByDebutBetween(Instant from, Instant to);

    Page<RendezVous> findByMedecinId(String medecinId, Pageable pageable);

    List<RendezVous> findByMedecinId(String medecinId);

    Page<RendezVous> findByPatientId(String patientId, Pageable pageable);

    List<RendezVous> findByMedecinIdAndDebutBetween(String medecinId, Instant from, Instant to);

    List<RendezVous> findByStatutAndDebutBetween(RendezVousStatut statut, Instant from, Instant to);

    List<RendezVous> findByMedecinIdAndStatut(String medecinId, RendezVousStatut statut);

    List<RendezVous> findByMedecinIdAndStatutIn(String medecinId, List<RendezVousStatut> statuts);

    long countByStatut(RendezVousStatut statut);

    Optional<RendezVous> findByIdAndMedecinId(String id, String medecinId);

    Optional<RendezVous> findByIdAndPatientId(String id, String patientId);
}
