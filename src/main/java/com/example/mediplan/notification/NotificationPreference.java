package com.example.mediplan.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "notification_preference")
public class NotificationPreference {

    @Id
    private String id;

    private String userId;

    private boolean emailEnabled;

    private boolean smsEnabled;

    private boolean pushEnabled;

    private boolean rappelAutomatique;
}
