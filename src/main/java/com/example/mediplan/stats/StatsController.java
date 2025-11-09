package com.example.mediplan.stats;

import com.example.mediplan.stats.dto.DashboardStatsResponse;
import com.example.mediplan.user.User;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class StatsController {

    private final StatsService service;

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public DashboardStatsResponse statsAdmin(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return service.statsAdmin(from, to);
    }

    @GetMapping("/admin/stats/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> exporterStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        DashboardStatsResponse stats = service.statsAdmin(from, to);
        StringBuilder csv = new StringBuilder();
        csv.append("periodeDebut,periodeFin,totalRendezVous,planifies,confirmes,annules,honores,patientsActifs,medecinsActifs\n");
        csv.append(stats.getPeriodeDebut()).append(',')
                .append(stats.getPeriodeFin()).append(',')
                .append(stats.getTotalRendezVous()).append(',')
                .append(stats.getRendezVousPlanifies()).append(',')
                .append(stats.getRendezVousConfirmes()).append(',')
                .append(stats.getRendezVousAnnules()).append(',')
                .append(stats.getRendezVousHonores()).append(',')
                .append(stats.getPatientsActifs()).append(',')
                .append(stats.getMedecinsActifs()).append('\n');
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=stats.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv.toString());
    }

    @GetMapping("/medecins/me/stats")
    @PreAuthorize("hasRole('MEDECIN')")
    public DashboardStatsResponse statsMedecin(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return service.statsMedecin(user.getId(), from, to);
    }
}
