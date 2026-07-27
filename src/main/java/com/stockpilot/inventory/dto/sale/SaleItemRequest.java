package com.stockpilot.inventory.dto.sale;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SaleItemRequest {
    @NotNull
    private Long productId;
    @NotNull @Min(1)
    private Integer quantity;
    private Double discount;
}
