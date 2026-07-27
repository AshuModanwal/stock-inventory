package com.stockpilot.inventory.dto.auth;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RegisterRequest {
    @NotBlank @Size(min = 2, max = 100)
    private String firstName;
    @Size(max = 100)
    private String lastName;
    @NotBlank @Email
    private String email;
    @NotBlank @Size(min = 8, max = 100)
    private String password;
    @Size(max = 15)
    private String phone;
    private Long companyId;
    private String roleName;  // COMPANY_ADMIN, SALESPERSON, VIEWER
}
