package com.stockpilot.inventory.dto.stock;

import com.stockpilot.inventory.enums.StockMovementType;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class StockAdjustmentRequest {
    @NotNull
    private Long productId;
    @NotNull
    private StockMovementType type;
    @NotNull @Min(1)
    private Integer quantity;
    private Double unitPrice;
    private String reason;
}
