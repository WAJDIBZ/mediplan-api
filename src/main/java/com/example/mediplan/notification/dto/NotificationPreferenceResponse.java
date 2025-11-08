package com.example.mediplan.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationPreferenceResponse {

    private final boolean emailEnabled;
    private final boolean smsEnabled;
    private final boolean pushEnabled;
    private final boolean rappelAutomatique;
}
