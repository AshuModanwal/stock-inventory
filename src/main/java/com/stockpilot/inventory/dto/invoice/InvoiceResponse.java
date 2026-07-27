package com.stockpilot.inventory.dto.invoice;

import com.stockpilot.inventory.dto.sale.SaleItemResponse;
import com.stockpilot.inventory.enums.InvoiceStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private Long saleId;
    private String saleNumber;
    private String companyName;
    private String companyAddress;
    private String companyGstin;
    private String companyPhone;
    private String customerName;
    private String customerAddress;
    private String customerGstin;
    private String customerPhone;
    private List<SaleItemResponse> items;
    private Double subtotal;
    private Double taxAmount;
    private Double discountAmount;
    private Double totalAmount;
    private InvoiceStatus status;
    private LocalDateTime dueDate;
    private LocalDateTime paidAt;
    private LocalDateTime generatedAt;
    private String pdfPath;
}
