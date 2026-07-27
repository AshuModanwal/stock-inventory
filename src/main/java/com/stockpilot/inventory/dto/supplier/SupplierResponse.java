package com.stockpilot.inventory.dto.supplier;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SupplierResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String gstin;
    private String contactPerson;
    private Long companyId;
    private Boolean active;
    private LocalDateTime createdAt;
}
