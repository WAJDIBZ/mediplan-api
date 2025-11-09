package com.example.mediplan.stats;

import com.example.mediplan.agenda.rendezvous.RendezVous;
import com.example.mediplan.agenda.rendezvous.RendezVousRepository;
import com.example.mediplan.agenda.rendezvous.RendezVousStatut;
import com.example.mediplan.stats.dto.DashboardStatsResponse;
import com.example.mediplan.user.Role;
import com.example.mediplan.user.UserRepository;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final RendezVousRepository rendezVousRepository;
    private final UserRepository userRepository;

    public DashboardStatsResponse statsAdmin(Instant from, Instant to) {
        Instant debut = from != null ? from : Instant.EPOCH;
        Instant fin = to != null ? to : Instant.now();
        List<RendezVous> rendezVous = (from != null || to != null)
                ? rendezVousRepository.findByDebutBetween(debut, fin)
                : rendezVousRepository.findAll();
        return buildResponse(debut, fin, rendezVous,
                userRepository.countByRoleAndActiveIsTrue(Role.PATIENT),
                userRepository.countByRoleAndActiveIsTrue(Role.MEDECIN));
    }

    public DashboardStatsResponse statsMedecin(@NotBlank String medecinId, Instant from, Instant to) {
        Instant debut = from != null ? from : Instant.EPOCH;
        Instant fin = to != null ? to : Instant.now();
        List<RendezVous> rendezVous = (from != null || to != null)
                ? rendezVousRepository.findByMedecinIdAndDebutBetween(medecinId, debut, fin)
                : rendezVousRepository.findByMedecinId(medecinId);
        return buildResponse(debut, fin, rendezVous, 0, 1);
    }

    private DashboardStatsResponse buildResponse(Instant debut, Instant fin, List<RendezVous> rendezVous,
            long patientsActifs, long medecinsActifs) {
        long total = rendezVous.size();
        long planifies = rendezVous.stream().filter(r -> r.getStatut() == RendezVousStatut.PLANIFIE).count();
        long confirmes = rendezVous.stream().filter(r -> r.getStatut() == RendezVousStatut.CONFIRME).count();
        long annules = rendezVous.stream().filter(r -> r.getStatut() == RendezVousStatut.ANNULE).count();
        long honores = rendezVous.stream().filter(r -> r.getStatut() == RendezVousStatut.HONORE).count();
        return DashboardStatsResponse.builder()
                .periodeDebut(debut)
                .periodeFin(fin)
                .totalRendezVous(total)
                .rendezVousPlanifies(planifies)
                .rendezVousConfirmes(confirmes)
                .rendezVousAnnules(annules)
                .rendezVousHonores(honores)
                .patientsActifs(patientsActifs)
                .medecinsActifs(medecinsActifs)
                .build();
    }
}
