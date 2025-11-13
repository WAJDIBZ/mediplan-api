package com.example.mediplan.agenda.rendezvous.dto;

import lombok.Data;

@Data
public class ParticipantDTO {
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String specialty;  // Only for medecin
}