package com.stockpilot.inventory.dto.stock;

import com.stockpilot.inventory.enums.StockMovementType;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockMovementResponse {
    private Long id;
    private Long productId;
    private String productName;
    private StockMovementType type;
    private Integer quantity;
    private Integer previousQty;
    private Integer newQty;
    private Double unitPrice;
    private String reason;
    private String referenceId;
    private String performedByName;
    private LocalDateTime movementDate;
}
