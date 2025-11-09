package com.example.mediplan.agenda.rendezvous;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.mediplan.agenda.disponibilite.Disponibilite;
import com.example.mediplan.agenda.disponibilite.DisponibiliteService;
import com.example.mediplan.agenda.disponibilite.Recurrence;
import com.example.mediplan.agenda.rendezvous.dto.RendezVousRequest;
import com.example.mediplan.common.exception.BusinessRuleException;
import com.example.mediplan.user.Medecin;
import com.example.mediplan.user.Patient;
import com.example.mediplan.user.Role;
import com.example.mediplan.user.User;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RendezVousServiceTest {

    @Mock
    private RendezVousRepository rendezVousRepository;

    @Mock
    private DisponibiliteService disponibiliteService;

    @InjectMocks
    private RendezVousService service;

    private User medecin;
    private User patient;

    @BeforeEach
    void setUp() {
        medecin = Medecin.builder()
                .id("med-1")
                .role(Role.MEDECIN)
                .build();
        patient = Patient.builder()
                .id("pat-1")
                .role(Role.PATIENT)
                .build();
    }

    @Test
    void creerRendezVous_okQuandDisponibiliteLibre() {
        Instant debut = Instant.parse("2024-10-15T08:30:00Z");
        Instant fin = Instant.parse("2024-10-15T09:00:00Z");
        when(disponibiliteService.trouverPourDate(eq(medecin.getId()), any(LocalDate.class)))
                .thenReturn(List.of(Disponibilite.builder()
                        .medecinId(medecin.getId())
                        .date(LocalDate.of(2024, 10, 15))
                        .heureDebut(LocalTime.of(8, 0))
                        .heureFin(LocalTime.of(12, 0))
                        .recurrence(Recurrence.AUCUNE)
                        .build()));
        when(rendezVousRepository.findByMedecinIdAndStatutIn(eq(medecin.getId()), any()))
                .thenReturn(List.of());
        when(rendezVousRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RendezVousRequest request = new RendezVousRequest();
        request.setMedecinId(medecin.getId());
        request.setPatientId(patient.getId());
        request.setDebut(debut);
        request.setFin(fin);
        request.setMotif("Contrôle");

        var response = service.creer(request, medecin);

        assertThat(response.getMedecinId()).isEqualTo(medecin.getId());
        assertThat(response.getPatientId()).isEqualTo(patient.getId());
        assertThat(response.getStatut()).isEqualTo(RendezVousStatut.PLANIFIE);
    }

    @Test
    void creerRendezVous_refuseConflit() {
        Instant debut = Instant.parse("2024-10-15T10:00:00Z");
        Instant fin = Instant.parse("2024-10-15T10:30:00Z");
        when(disponibiliteService.trouverPourDate(eq(medecin.getId()), any(LocalDate.class)))
                .thenReturn(List.of(Disponibilite.builder()
                        .medecinId(medecin.getId())
                        .date(LocalDate.of(2024, 10, 15))
                        .heureDebut(LocalTime.of(8, 0))
                        .heureFin(LocalTime.of(18, 0))
                        .recurrence(Recurrence.AUCUNE)
                        .build()));
        RendezVous existant = RendezVous.builder()
                .id("rdv-1")
                .medecinId(medecin.getId())
                .patientId(patient.getId())
                .debut(Instant.parse("2024-10-15T10:00:00Z"))
                .fin(Instant.parse("2024-10-15T10:30:00Z"))
                .statut(RendezVousStatut.CONFIRME)
                .build();
        when(rendezVousRepository.findByMedecinIdAndStatutIn(eq(medecin.getId()), any()))
                .thenReturn(List.of(existant));

        RendezVousRequest request = new RendezVousRequest();
        request.setMedecinId(medecin.getId());
        request.setPatientId(patient.getId());
        request.setDebut(debut);
        request.setFin(fin);

        assertThatThrownBy(() -> service.creer(request, medecin))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Le créneau est déjà occupé");
    }
}
