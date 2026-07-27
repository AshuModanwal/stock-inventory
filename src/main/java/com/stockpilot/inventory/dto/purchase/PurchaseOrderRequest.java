package com.stockpilot.inventory.dto.purchase;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PurchaseOrderRequest {
    @NotNull
    private Long supplierId;
    @NotEmpty
    private List<PurchaseOrderItemRequest> items;
    private LocalDate expectedDate;
    private String notes;
}
