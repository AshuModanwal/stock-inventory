package com.stockpilot.inventory.dto.sale;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SaleItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Integer quantity;
    private Double unitPrice;
    private Double costPrice;
    private Double discount;
    private Double taxRate;
    private Double taxAmount;
    private Double totalPrice;
}
