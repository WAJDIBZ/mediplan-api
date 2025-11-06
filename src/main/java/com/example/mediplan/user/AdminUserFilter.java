package com.example.mediplan.user;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdminUserFilter {
    String query;
    Role role;
    Boolean active;
    String provider;
}
