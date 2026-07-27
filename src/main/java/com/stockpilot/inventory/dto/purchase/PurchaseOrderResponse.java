package com.stockpilot.inventory.dto.purchase;

import com.stockpilot.inventory.enums.PurchaseOrderStatus;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PurchaseOrderResponse {
    private Long id;
    private String poNumber;
    private Long supplierId;
    private String supplierName;
    private List<PurchaseOrderItemResponse> items;
    private Double subtotal;
    private Double taxAmount;
    private Double totalAmount;
    private PurchaseOrderStatus status;
    private LocalDate expectedDate;
    private LocalDate receivedDate;
    private String notes;
    private Long companyId;
    private LocalDateTime createdAt;
}
