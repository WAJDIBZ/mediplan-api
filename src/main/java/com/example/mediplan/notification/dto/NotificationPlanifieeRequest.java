package com.example.mediplan.notification.dto;

import com.example.mediplan.notification.CanalNotification;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationPlanifieeRequest {

    @NotBlank(message = "Le rendez-vous est obligatoire")
    private String rendezVousId;

    @NotBlank(message = "Le destinataire est obligatoire")
    private String destinataireId;

    @NotNull(message = "Le canal est obligatoire")
    private CanalNotification canal;

    @NotNull(message = "La date d'envoi est obligatoire")
    @Future(message = "La date d'envoi doit être dans le futur")
    private Instant dateEnvoi;

    @Size(max = 500, message = "Le message ne doit pas dépasser 500 caractères")
    private String message;
}
