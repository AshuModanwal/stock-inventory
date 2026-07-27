package com.stockpilot.inventory.dto.company;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CompanyRequest {
    @NotBlank @Size(max = 150)
    private String name;
    @Size(max = 150)
    private String displayName;
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
    @Size(max = 15)
    private String pan;
    @Size(max = 255)
    private String website;
    @Size(max = 10)
    private String invoicePrefix;
    private Double taxRate;
}
