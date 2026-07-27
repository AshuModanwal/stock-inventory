package com.stockpilot.inventory.dto.purchase;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PurchaseOrderItemRequest {
    @NotNull
    private Long productId;
    @NotNull @Min(1)
    private Integer quantity;
    @NotNull @Positive
    private Double unitPrice;
}
