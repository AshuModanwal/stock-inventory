package com.stockpilot.inventory.dto.user;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private Long companyId;
    private String companyName;
    private Set<String> roles;
    private Boolean active;
    private Boolean emailVerified;
    private String avatar;
    private LocalDateTime createdAt;
}
