package com.stockpilot.inventory.dto.sale;

import com.stockpilot.inventory.enums.PaymentMethod;
import com.stockpilot.inventory.enums.SaleStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SaleResponse {
    private Long id;
    private String saleNumber;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private List<SaleItemResponse> items;
    private Double subtotal;
    private Double taxAmount;
    private Double discountAmount;
    private Double totalAmount;
    private PaymentMethod paymentMethod;
    private String notes;
    private SaleStatus status;
    private Long soldById;
    private String soldByName;
    private Long companyId;
    private LocalDateTime saleDate;
    private Long invoiceId;
    private String invoiceNumber;
}
