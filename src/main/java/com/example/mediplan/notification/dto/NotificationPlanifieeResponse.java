package com.example.mediplan.notification.dto;

import com.example.mediplan.notification.CanalNotification;
import com.example.mediplan.notification.StatutNotification;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationPlanifieeResponse {

    private final String id;
    private final String rendezVousId;
    private final String destinataireId;
    private final CanalNotification canal;
    private final Instant dateEnvoi;
    private final String message;
    private final StatutNotification statut;
    private final Instant createdAt;
    private final Instant updatedAt;
}
