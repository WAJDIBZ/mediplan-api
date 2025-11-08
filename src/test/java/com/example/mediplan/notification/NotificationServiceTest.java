package com.example.mediplan.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.mediplan.notification.dto.NotificationPlanifieeRequest;
import com.example.mediplan.notification.dto.NotificationPreferenceRequest;
import com.example.mediplan.common.exception.BusinessRuleException;
import com.example.mediplan.user.Patient;
import com.example.mediplan.user.Role;
import com.example.mediplan.user.User;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @Mock
    private NotificationPlanifieeRepository planifieeRepository;

    @InjectMocks
    private NotificationService service;

    private User patient;

    @BeforeEach
    void setup() {
        patient = Patient.builder().id("pat-1").role(Role.PATIENT).build();
    }

    @Test
    void updatePreferences_creeSiAbsent() {
        when(preferenceRepository.findByUserId(patient.getId())).thenReturn(Optional.empty());
        when(preferenceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationPreferenceRequest request = new NotificationPreferenceRequest();
        request.setEmailEnabled(false);
        request.setSmsEnabled(true);
        request.setPushEnabled(false);
        request.setRappelAutomatique(true);

        var response = service.updatePreferences(patient, request);

        assertThat(response.isSmsEnabled()).isTrue();
        assertThat(response.isEmailEnabled()).isFalse();
    }

    @Test
    void planifier_refusePatientPourAutrui() {
        NotificationPlanifieeRequest request = new NotificationPlanifieeRequest();
        request.setRendezVousId("rdv-1");
        request.setDestinataireId("pat-2");
        request.setCanal(CanalNotification.EMAIL);
        request.setDateEnvoi(Instant.now().plusSeconds(3600));

        assertThatThrownBy(() -> service.planifier(request, patient))
                .isInstanceOf(BusinessRuleException.class);
    }
}
