package com.stockpilot.inventory.dto.customer;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CustomerRequest {
    @NotBlank @Size(max = 150)
    private String name;
    @Email @Size(max = 150)
    private String email;
    @Size(max = 15)
    private String phone;
    private String address;
    @Size(max = 100)
    private String city;
    @Size(max = 100)
    private String state;
    @Size(max = 10)
    private String pincode;
    @Size(max = 20)
    private String gstin;
}
