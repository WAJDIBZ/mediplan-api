package com.example.mediplan.user;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EmergencyContact {
    private String name;
    private String phone;
    private String relation;
}
