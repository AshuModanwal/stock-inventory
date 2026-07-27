package com.stockpilot.inventory.dto.product;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String sku;
    private String barcode;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Double costPrice;
    private Double sellPrice;
    private Double mrp;
    private Integer quantity;
    private Integer lowStockThreshold;
    private String unit;
    private Double taxRate;
    private String hsnCode;
    private String image;
    private Long companyId;
    private Boolean active;
    private Boolean lowStock;
    private Boolean outOfStock;
    private Double profitMargin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
