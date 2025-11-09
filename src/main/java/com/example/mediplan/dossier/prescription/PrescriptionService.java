package com.example.mediplan.dossier.prescription;

import com.example.mediplan.common.exception.BusinessRuleException;
import com.example.mediplan.common.exception.ResourceNotFoundException;
import com.example.mediplan.dossier.consultation.Consultation;
import com.example.mediplan.dossier.consultation.ConsultationRepository;
import com.example.mediplan.dossier.prescription.dto.PrescriptionRequest;
import com.example.mediplan.dossier.prescription.dto.PrescriptionResponse;
import com.example.mediplan.user.Role;
import com.example.mediplan.user.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class PrescriptionService {

    private final PrescriptionRepository repository;
    private final ConsultationRepository consultationRepository;

    public PrescriptionResponse creer(@Valid PrescriptionRequest request, User acteur) {
        Consultation consultation = consultationRepository.findById(request.getConsultationId())
                .orElseThrow(() -> new ResourceNotFoundException("Consultation introuvable"));
        if (acteur.getRole() == Role.MEDECIN && !acteur.getId().equals(consultation.getMedecinId())) {
            throw new BusinessRuleException("Vous ne pouvez prescrire que pour vos consultations");
        }
        if (acteur.getRole() == Role.PATIENT) {
            throw new BusinessRuleException("Le patient ne peut pas créer de prescription");
        }
        if (!consultation.getPatientId().equals(request.getPatientId())) {
            throw new BusinessRuleException("Le patient ne correspond pas à la consultation");
        }
        if (repository.existsByConsultationId(consultation.getId())) {
            throw new BusinessRuleException("Une prescription existe déjà pour cette consultation");
        }
        Prescription prescription = Prescription.builder()
                .consultationId(consultation.getId())
                .medecinId(consultation.getMedecinId())
                .patientId(consultation.getPatientId())
                .medicaments(request.getMedicaments().stream().map(PrescriptionRequest.MedicationRequest::toMedication)
                        .collect(Collectors.toList()))
                .instructionsGenerales(request.getInstructionsGenerales())
                .build();
        return toResponse(repository.save(prescription));
    }

    public Page<PrescriptionResponse> listerPourPatient(@NotBlank String patientId, Pageable pageable) {
        return repository.findByPatientIdOrderByCreatedAtDesc(patientId, pageable)
                .map(this::toResponse);
    }

    public Page<PrescriptionResponse> listerPourMedecin(@NotBlank String medecinId, Pageable pageable) {
        return repository.findByMedecinIdOrderByCreatedAtDesc(medecinId, pageable)
                .map(this::toResponse);
    }

    public Page<PrescriptionResponse> listerTous(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    public PrescriptionResponse consulter(@NotBlank String id, User acteur) {
        Prescription prescription = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription introuvable"));
        if (acteur.getRole() == Role.ADMIN) {
            return toResponse(prescription);
        }
        if (acteur.getRole() == Role.MEDECIN && !acteur.getId().equals(prescription.getMedecinId())) {
            throw new BusinessRuleException("Accès refusé");
        }
        if (acteur.getRole() == Role.PATIENT && !acteur.getId().equals(prescription.getPatientId())) {
            throw new BusinessRuleException("Accès refusé");
        }
        return toResponse(prescription);
    }

    private PrescriptionResponse toResponse(Prescription prescription) {
        return PrescriptionResponse.builder()
                .id(prescription.getId())
                .consultationId(prescription.getConsultationId())
                .medecinId(prescription.getMedecinId())
                .patientId(prescription.getPatientId())
                .medicaments(prescription.getMedicaments())
                .instructionsGenerales(prescription.getInstructionsGenerales())
                .createdAt(prescription.getCreatedAt())
                .build();
    }
}
