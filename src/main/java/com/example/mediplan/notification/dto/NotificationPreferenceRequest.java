package com.example.mediplan.notification.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationPreferenceRequest {

    private boolean emailEnabled;
    private boolean smsEnabled;
    private boolean pushEnabled;
    private boolean rappelAutomatique;
}
