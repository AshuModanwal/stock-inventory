package com.stockpilot.inventory.entity;

import com.stockpilot.inventory.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"sku", "company_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 50)
    private String sku;

    @Column(length = 100)
    private String barcode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "cost_price", nullable = false)
    private Double costPrice;

    @Column(name = "sell_price", nullable = false)
    private Double sellPrice;

    @Column(name = "mrp")
    private Double mrp;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(name = "low_stock_threshold")
    @Builder.Default
    private Integer lowStockThreshold = 5;

    @Column(length = 30)
    @Builder.Default
    private String unit = "PCS";

    @Column(name = "tax_rate")
    @Builder.Default
    private Double taxRate = 18.0;

    @Column(name = "hsn_code", length = 20)
    private String hsnCode;

    @Column(length = 500)
    private String image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    public boolean isLowStock() {
        return quantity <= lowStockThreshold;
    }

    public boolean isOutOfStock() {
        return quantity <= 0;
    }
}
