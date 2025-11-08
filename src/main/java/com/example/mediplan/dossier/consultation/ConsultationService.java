package com.example.mediplan.dossier.consultation;

import com.example.mediplan.agenda.rendezvous.RendezVous;
import com.example.mediplan.agenda.rendezvous.RendezVousRepository;
import com.example.mediplan.agenda.rendezvous.RendezVousStatut;
import com.example.mediplan.common.exception.BusinessRuleException;
import com.example.mediplan.common.exception.ResourceNotFoundException;
import com.example.mediplan.dossier.consultation.dto.ConsultationRequest;
import com.example.mediplan.dossier.consultation.dto.ConsultationResponse;
import com.example.mediplan.user.Role;
import com.example.mediplan.user.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class ConsultationService {

    private final ConsultationRepository repository;
    private final RendezVousRepository rendezVousRepository;

    public ConsultationResponse creer(@Valid ConsultationRequest request, User acteur) {
        RendezVous rendezVous = rendezVousRepository.findById(request.getRendezVousId())
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous introuvable"));
        if (acteur.getRole() == Role.MEDECIN && !acteur.getId().equals(rendezVous.getMedecinId())) {
            throw new BusinessRuleException("Vous ne pouvez documenter que vos propres rendez-vous");
        }
        if (acteur.getRole() == Role.PATIENT) {
            throw new BusinessRuleException("Le patient ne peut pas créer de consultation");
        }
        if (!rendezVous.getPatientId().equals(request.getPatientId())) {
            throw new BusinessRuleException("Le patient indiqué ne correspond pas au rendez-vous");
        }
        if (rendezVous.getStatut() == RendezVousStatut.ANNULE) {
            throw new BusinessRuleException("Impossible de documenter un rendez-vous annulé");
        }
        Consultation consultation = Consultation.builder()
                .rendezVousId(rendezVous.getId())
                .medecinId(rendezVous.getMedecinId())
                .patientId(rendezVous.getPatientId())
                .date(request.getDate())
                .resume(request.getResume())
                .diagnostic(request.getDiagnostic())
                .planSuivi(request.getPlanSuivi())
                .recommandations(request.getRecommandations())
                .build();
        return toResponse(repository.save(consultation));
    }

    public Page<ConsultationResponse> listerPourPatient(@NotBlank String patientId, Pageable pageable) {
        return repository.findByPatientIdOrderByDateDesc(patientId, pageable)
                .map(this::toResponse);
    }

    public Page<ConsultationResponse> listerPourMedecin(@NotBlank String medecinId, Pageable pageable) {
        return repository.findByMedecinIdOrderByDateDesc(medecinId, pageable)
                .map(this::toResponse);
    }

    public Page<ConsultationResponse> listerTous(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    public ConsultationResponse consulter(@NotBlank String id, User acteur) {
        Consultation consultation = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation introuvable"));
        if (acteur.getRole() == Role.ADMIN) {
            return toResponse(consultation);
        }
        if (acteur.getRole() == Role.MEDECIN && !acteur.getId().equals(consultation.getMedecinId())) {
            throw new BusinessRuleException("Accès refusé");
        }
        if (acteur.getRole() == Role.PATIENT && !acteur.getId().equals(consultation.getPatientId())) {
            throw new BusinessRuleException("Accès refusé");
        }
        return toResponse(consultation);
    }

    private ConsultationResponse toResponse(Consultation consultation) {
        return ConsultationResponse.builder()
                .id(consultation.getId())
                .rendezVousId(consultation.getRendezVousId())
                .medecinId(consultation.getMedecinId())
                .patientId(consultation.getPatientId())
                .date(consultation.getDate())
                .resume(consultation.getResume())
                .diagnostic(consultation.getDiagnostic())
                .planSuivi(consultation.getPlanSuivi())
                .recommandations(consultation.getRecommandations())
                .createdAt(consultation.getCreatedAt())
                .build();
    }
}
