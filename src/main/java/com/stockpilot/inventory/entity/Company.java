package com.stockpilot.inventory.entity;

import com.stockpilot.inventory.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "companies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "display_name", length = 150)
    private String displayName;

    @Column(unique = true, length = 150)
    private String email;

    @Column(length = 15)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 10)
    private String pincode;

    @Column(length = 20)
    private String gstin;

    @Column(length = 15)
    private String pan;

    @Column(length = 500)
    private String logo;

    @Column(length = 255)
    private String website;

    @Column(name = "invoice_prefix", length = 10)
    private String invoicePrefix;

    @Column(name = "next_invoice_number")
    @Builder.Default
    private Long nextInvoiceNumber = 1L;

    @Column(name = "tax_rate")
    @Builder.Default
    private Double taxRate = 18.0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
