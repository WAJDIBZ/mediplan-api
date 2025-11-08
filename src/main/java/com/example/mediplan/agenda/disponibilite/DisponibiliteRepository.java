package com.example.mediplan.agenda.disponibilite;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisponibiliteRepository extends MongoRepository<Disponibilite, String> {

    List<Disponibilite> findByMedecinIdAndDateBetweenAndActifIsTrue(String medecinId, LocalDate from, LocalDate to);

    List<Disponibilite> findByMedecinIdAndDateAndActifIsTrue(String medecinId, LocalDate date);

    List<Disponibilite> findByMedecinIdAndActifIsTrue(String medecinId);

    Optional<Disponibilite> findByIdAndMedecinId(String id, String medecinId);
}
