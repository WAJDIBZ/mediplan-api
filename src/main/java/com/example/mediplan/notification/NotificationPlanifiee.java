package com.example.mediplan.notification;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "notification_planifiee")
public class NotificationPlanifiee {

    @Id
    private String id;

    private String rendezVousId;

    private String destinataireId;

    private CanalNotification canal;

    private Instant dateEnvoi;

    private String message;

    private StatutNotification statut;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
