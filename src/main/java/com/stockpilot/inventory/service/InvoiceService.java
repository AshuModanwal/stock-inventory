package com.stockpilot.inventory.service;

import com.stockpilot.inventory.dto.common.PagedResponse;
import com.stockpilot.inventory.dto.invoice.InvoiceResponse;
import com.stockpilot.inventory.dto.sale.SaleItemResponse;
import com.stockpilot.inventory.entity.*;
import com.stockpilot.inventory.enums.InvoiceStatus;
import com.stockpilot.inventory.exception.ResourceNotFoundException;
import com.stockpilot.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CompanyRepository companyRepository;
    private final SaleItemRepository saleItemRepository;

    @Transactional
    public Invoice generateInvoice(Sale sale, Company ignoredCompany, Customer customer) {
        // Generate invoice number: PREFIX-YYMM-XXXXXX
        Company company = companyRepository.findByIdForUpdate(sale.getCompany().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company", "id", sale.getCompany().getId()));

        String prefix = company.getInvoicePrefix() != null
                ? company.getInvoicePrefix()
                : "INV";

        String dateStr = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyMM"));

        Long nextNumber = company.getNextInvoiceNumber();

        String invoiceNumber = String.format(
                "%s-%s-%06d",
                prefix,
                dateStr,
                nextNumber
        );

        company.setNextInvoiceNumber(nextNumber + 1);

        companyRepository.save(company);

        // Build address strings
        String companyAddress = buildAddress(company.getAddress(), company.getCity(), company.getState(), company.getPincode());
        String customerAddress = customer != null
                ? buildAddress(customer.getAddress(), customer.getCity(), customer.getState(), customer.getPincode())
                : null;

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .sale(sale)
                .company(company)
                .companyName(company.getDisplayName() != null ? company.getDisplayName() : company.getName())
                .companyAddress(companyAddress)
                .companyGstin(company.getGstin())
                .companyPhone(company.getPhone())
                .customerName(sale.getCustomerName())
                .customerAddress(customerAddress)
                .customerGstin(customer != null ? customer.getGstin() : null)
                .customerPhone(sale.getCustomerPhone())
                .subtotal(sale.getSubtotal())
                .taxAmount(sale.getTaxAmount())
                .discountAmount(sale.getDiscountAmount())
                .totalAmount(sale.getTotalAmount())
                .status(InvoiceStatus.GENERATED)
                .generatedAt(LocalDateTime.now())
                .build();

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice generated: {} for sale {}", invoiceNumber, sale.getSaleNumber());
        return saved;
    }

    @Transactional(readOnly = true)
    public PagedResponse<InvoiceResponse> getAll(Long companyId, int page, int size) {
        Page<Invoice> invoices = invoiceRepository.findByCompanyIdOrderByGeneratedAtDesc(
                companyId, PageRequest.of(page, size));
        return PagedResponse.<InvoiceResponse>builder()
                .content(invoices.getContent().stream().map(this::mapToResponse).toList())
                .page(invoices.getNumber()).size(invoices.getSize())
                .totalElements(invoices.getTotalElements()).totalPages(invoices.getTotalPages())
                .last(invoices.isLast()).build();
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getById(Long id, Long companyId) {
        Invoice invoice = invoiceRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        return mapToResponse(invoice);
    }

    @Transactional
    public InvoiceResponse markAsPaid(Long id, Long companyId) {
        Invoice invoice = invoiceRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        return mapToResponse(invoiceRepository.save(invoice));
    }

    public Optional<Invoice> findBySaleId(Long saleId) {
        return invoiceRepository.findBySaleId(saleId);
    }

    private InvoiceResponse mapToResponse(Invoice inv) {
        var items = saleItemRepository.findBySaleId(inv.getSale().getId()).stream()
                .map(i -> SaleItemResponse.builder()
                        .id(i.getId()).productId(i.getProduct().getId())
                        .productName(i.getProductName()).productSku(i.getProductSku())
                        .quantity(i.getQuantity()).unitPrice(i.getUnitPrice())
                        .discount(i.getDiscount()).taxRate(i.getTaxRate())
                        .taxAmount(i.getTaxAmount()).totalPrice(i.getTotalPrice()).build())
                .toList();

        return InvoiceResponse.builder()
                .id(inv.getId()).invoiceNumber(inv.getInvoiceNumber())
                .saleId(inv.getSale().getId()).saleNumber(inv.getSale().getSaleNumber())
                .companyName(inv.getCompanyName()).companyAddress(inv.getCompanyAddress())
                .companyGstin(inv.getCompanyGstin()).companyPhone(inv.getCompanyPhone())
                .customerName(inv.getCustomerName()).customerAddress(inv.getCustomerAddress())
                .customerGstin(inv.getCustomerGstin()).customerPhone(inv.getCustomerPhone())
                .items(items).subtotal(inv.getSubtotal()).taxAmount(inv.getTaxAmount())
                .discountAmount(inv.getDiscountAmount()).totalAmount(inv.getTotalAmount())
                .status(inv.getStatus()).dueDate(inv.getDueDate()).paidAt(inv.getPaidAt())
                .generatedAt(inv.getGeneratedAt()).pdfPath(inv.getPdfPath()).build();
    }

    private String buildAddress(String address, String city, String state, String pincode) {
        StringBuilder sb = new StringBuilder();
        if (address != null) sb.append(address);
        if (city != null) sb.append(sb.length() > 0 ? ", " : "").append(city);
        if (state != null) sb.append(sb.length() > 0 ? ", " : "").append(state);
        if (pincode != null) sb.append(sb.length() > 0 ? " - " : "").append(pincode);
        return sb.toString();
    }
}
