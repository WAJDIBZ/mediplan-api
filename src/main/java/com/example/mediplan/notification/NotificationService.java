package com.example.mediplan.notification;

import com.example.mediplan.common.exception.BusinessRuleException;
import com.example.mediplan.common.exception.ResourceNotFoundException;
import com.example.mediplan.notification.dto.NotificationPlanifieeRequest;
import com.example.mediplan.notification.dto.NotificationPlanifieeResponse;
import com.example.mediplan.notification.dto.NotificationPreferenceRequest;
import com.example.mediplan.notification.dto.NotificationPreferenceResponse;
import com.example.mediplan.user.Role;
import com.example.mediplan.user.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class NotificationService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationPlanifieeRepository planifieeRepository;

    public NotificationPreferenceResponse getPreferences(User user) {
        return preferenceRepository.findByUserId(user.getId())
                .map(this::toResponse)
                .orElse(NotificationPreferenceResponse.builder()
                        .emailEnabled(true)
                        .smsEnabled(false)
                        .pushEnabled(false)
                        .rappelAutomatique(true)
                        .build());
    }

    public NotificationPreferenceResponse updatePreferences(User user,
            @Valid NotificationPreferenceRequest request) {
        NotificationPreference preference = preferenceRepository.findByUserId(user.getId())
                .orElse(NotificationPreference.builder().userId(user.getId()).build());
        preference.setEmailEnabled(request.isEmailEnabled());
        preference.setSmsEnabled(request.isSmsEnabled());
        preference.setPushEnabled(request.isPushEnabled());
        preference.setRappelAutomatique(request.isRappelAutomatique());
        return toResponse(preferenceRepository.save(preference));
    }

    public NotificationPlanifieeResponse planifier(@Valid NotificationPlanifieeRequest request, User acteur) {
        if (acteur.getRole() == Role.PATIENT && !acteur.getId().equals(request.getDestinataireId())) {
            throw new BusinessRuleException("Le patient ne peut planifier que pour lui-même");
        }
        NotificationPlanifiee notification = NotificationPlanifiee.builder()
                .rendezVousId(request.getRendezVousId())
                .destinataireId(request.getDestinataireId())
                .canal(request.getCanal())
                .dateEnvoi(request.getDateEnvoi())
                .message(request.getMessage())
                .statut(StatutNotification.PLANIFIEE)
                .build();
        return toResponse(planifieeRepository.save(notification));
    }

    public Page<NotificationPlanifieeResponse> lister(Pageable pageable) {
        return planifieeRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public NotificationPlanifieeResponse marquerEnvoyee(@NotBlank String id, boolean succes) {
        NotificationPlanifiee notification = planifieeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable"));
        notification.setStatut(succes ? StatutNotification.ENVOYEE : StatutNotification.ECHEC);
        return toResponse(planifieeRepository.save(notification));
    }

    public List<NotificationPlanifieeResponse> declencherRappels() {
        return planifieeRepository.findByDateEnvoiBeforeAndStatut(Instant.now(), StatutNotification.PLANIFIEE)
                .stream()
                .map(notification -> {
                    notification.setStatut(StatutNotification.ENVOYEE);
                    return toResponse(planifieeRepository.save(notification));
                })
                .collect(Collectors.toList());
    }

    private NotificationPreferenceResponse toResponse(NotificationPreference preference) {
        return NotificationPreferenceResponse.builder()
                .emailEnabled(preference.isEmailEnabled())
                .smsEnabled(preference.isSmsEnabled())
                .pushEnabled(preference.isPushEnabled())
                .rappelAutomatique(preference.isRappelAutomatique())
                .build();
    }

    private NotificationPlanifieeResponse toResponse(NotificationPlanifiee notification) {
        return NotificationPlanifieeResponse.builder()
                .id(notification.getId())
                .rendezVousId(notification.getRendezVousId())
                .destinataireId(notification.getDestinataireId())
                .canal(notification.getCanal())
                .dateEnvoi(notification.getDateEnvoi())
                .message(notification.getMessage())
                .statut(notification.getStatut())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}
