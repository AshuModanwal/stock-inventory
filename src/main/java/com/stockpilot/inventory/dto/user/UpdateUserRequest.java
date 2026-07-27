package com.stockpilot.inventory.dto.user;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UpdateUserRequest {
    @Size(min = 2, max = 100)
    private String firstName;
    @Size(max = 100)
    private String lastName;
    @Size(max = 15)
    private String phone;
    private Boolean active;
    private String roleName;
}
