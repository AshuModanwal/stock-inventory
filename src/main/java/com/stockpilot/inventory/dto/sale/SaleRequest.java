package com.stockpilot.inventory.dto.sale;

import com.stockpilot.inventory.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SaleRequest {
    private Long customerId;
    private String customerName;
    private String customerPhone;
    @NotEmpty
    private List<SaleItemRequest> items;
    private Double discountAmount;
    private PaymentMethod paymentMethod;
    private String notes;
}
