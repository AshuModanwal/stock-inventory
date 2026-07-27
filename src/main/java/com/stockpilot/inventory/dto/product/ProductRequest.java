package com.stockpilot.inventory.dto.product;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProductRequest {
    @NotBlank @Size(max = 200)
    private String name;
    @NotBlank @Size(max = 50)
    private String sku;
    @Size(max = 100)
    private String barcode;
    private String description;
    private Long categoryId;
    @NotNull @Positive
    private Double costPrice;
    @NotNull @Positive
    private Double sellPrice;
    private Double mrp;
    @NotNull @Min(0)
    private Integer quantity;
    @Min(0)
    private Integer lowStockThreshold;
    @Size(max = 30)
    private String unit;
    private Double taxRate;
    @Size(max = 20)
    private String hsnCode;
}
