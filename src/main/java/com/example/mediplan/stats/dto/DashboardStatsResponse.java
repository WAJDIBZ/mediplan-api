package com.example.mediplan.stats.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardStatsResponse {

    private final Instant periodeDebut;
    private final Instant periodeFin;
    private final long totalRendezVous;
    private final long rendezVousPlanifies;
    private final long rendezVousConfirmes;
    private final long rendezVousAnnules;
    private final long rendezVousHonores;
    private final long patientsActifs;
    private final long medecinsActifs;
}
