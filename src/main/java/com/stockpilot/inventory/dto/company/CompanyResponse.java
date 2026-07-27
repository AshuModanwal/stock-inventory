package com.stockpilot.inventory.dto.company;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CompanyResponse {
    private Long id;
    private String name;
    private String displayName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String gstin;
    private String pan;
    private String logo;
    private String website;
    private String invoicePrefix;
    private Double taxRate;
    private Boolean active;
    private LocalDateTime createdAt;
}
