package com.stockpilot.inventory.service;

import com.stockpilot.inventory.dto.common.PagedResponse;
import com.stockpilot.inventory.dto.sale.*;
import com.stockpilot.inventory.entity.*;
import com.stockpilot.inventory.enums.*;
import com.stockpilot.inventory.exception.*;
import com.stockpilot.inventory.repository.*;
import com.stockpilot.inventory.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final StockMovementRepository stockMovementRepository;
    private final InvoiceService invoiceService;
    private final NotificationService notificationService;

    @Transactional
    public SaleResponse createSale(SaleRequest request, UserPrincipal currentUser) {
        Company company = companyRepository.findById(currentUser.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", currentUser.getCompanyId()));
        User seller = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        // Resolve customer
        Customer customer = null;
        String customerName = request.getCustomerName();
        String customerPhone = request.getCustomerPhone();
        if (request.getCustomerId() != null) {
            customer = customerRepository.findByIdAndCompanyId(request.getCustomerId(), company.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));
            customerName = customer.getName();
            customerPhone = customer.getPhone();
        }

        // Generate sale number: SALE-YYYYMMDD-XXXXX
        String saleNumber = "SALE-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%05d", saleRepository.countByCompanyId(company.getId()) + 1);

        Sale sale = Sale.builder()
                .saleNumber(saleNumber)
                .customer(customer)
                .customerName(customerName != null ? customerName : "Walk-in Customer")
                .customerPhone(customerPhone)
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.CASH)
                .notes(request.getNotes())
                .soldBy(seller)
                .company(company)
                .saleDate(LocalDateTime.now())
                .status(SaleStatus.COMPLETED)
                .subtotal(0.0)
                .taxAmount(0.0)
                .discountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : 0.0)
                .totalAmount(0.0)
                .build();

        double subtotal = 0;
        double totalTax = 0;
        List<SaleItem> saleItems = new ArrayList<>();

        for (SaleItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findByIdAndCompanyId(itemReq.getProductId(), company.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemReq.getProductId()));

            if (product.getQuantity() < itemReq.getQuantity()) {
                throw new BadRequestException("Insufficient stock for '" + product.getName()
                        + "'. Available: " + product.getQuantity() + ", Requested: " + itemReq.getQuantity());
            }

            double itemDiscount = itemReq.getDiscount() != null ? itemReq.getDiscount() : 0.0;
            double lineTotal = (product.getSellPrice() * itemReq.getQuantity()) - itemDiscount;
            double taxAmount = lineTotal * (product.getTaxRate() / 100.0);

            SaleItem saleItem = SaleItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .productSku(product.getSku())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getSellPrice())
                    .costPrice(product.getCostPrice())
                    .discount(itemDiscount)
                    .taxRate(product.getTaxRate())
                    .taxAmount(taxAmount)
                    .totalPrice(lineTotal + taxAmount)
                    .build();

            saleItems.add(saleItem);
            subtotal += lineTotal;
            totalTax += taxAmount;

            // Deduct stock
            int prevQty = product.getQuantity();
            product.setQuantity(prevQty - itemReq.getQuantity());
            productRepository.save(product);

            // Record stock movement
            StockMovement movement = StockMovement.builder()
                    .product(product)
                    .type(StockMovementType.SALE)
                    .quantity(itemReq.getQuantity())
                    .previousQty(prevQty)
                    .newQty(product.getQuantity())
                    .unitPrice(product.getSellPrice())
                    .referenceId(saleNumber)
                    .performedBy(seller)
                    .company(company)
                    .build();
            stockMovementRepository.save(movement);

            // Check low stock notification
            if (product.isLowStock()) {
                notificationService.createLowStockAlert(product, company);
            }
        }

        double discountAmount = sale.getDiscountAmount();
        double totalAmount = subtotal + totalTax - discountAmount;

        sale.setSubtotal(subtotal);
        sale.setTaxAmount(totalTax);
        sale.setTotalAmount(totalAmount);

        sale = saleRepository.save(sale);

        for (SaleItem item : saleItems) {
            item.setSale(sale);
        }
        saleItemRepository.saveAll(saleItems);

        // AUTO-GENERATE INVOICE
        Invoice invoice = invoiceService.generateInvoice(sale, company, customer);

        log.info("Sale completed: {} | Amount: {} | Invoice: {}", saleNumber, totalAmount, invoice.getInvoiceNumber());

        SaleResponse response = mapToResponse(sale);
        response.setItems(saleItems.stream().map(this::mapItemToResponse).toList());
        response.setInvoiceId(invoice.getId());
        response.setInvoiceNumber(invoice.getInvoiceNumber());
        return response;
    }

    @Transactional(readOnly = true)
    public PagedResponse<SaleResponse> getAll(Long companyId, int page, int size) {
        Page<Sale> sales = saleRepository.findByCompanyIdOrderBySaleDateDesc(companyId, PageRequest.of(page, size));
        return PagedResponse.<SaleResponse>builder()
                .content(sales.getContent().stream().map(s -> {
                    SaleResponse r = mapToResponse(s);
                    r.setItems(saleItemRepository.findBySaleId(s.getId()).stream().map(this::mapItemToResponse).toList());
                    return r;
                }).toList())
                .page(sales.getNumber()).size(sales.getSize())
                .totalElements(sales.getTotalElements()).totalPages(sales.getTotalPages())
                .last(sales.isLast()).build();
    }

    @Transactional(readOnly = true)
    public SaleResponse getById(Long id, Long companyId) {
        Sale sale = saleRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", id));
        SaleResponse response = mapToResponse(sale);
        response.setItems(saleItemRepository.findBySaleId(sale.getId()).stream().map(this::mapItemToResponse).toList());

        // Attach invoice info
        invoiceService.findBySaleId(sale.getId()).ifPresent(inv -> {
            response.setInvoiceId(inv.getId());
            response.setInvoiceNumber(inv.getInvoiceNumber());
        });

        return response;
    }

    private SaleResponse mapToResponse(Sale s) {
        return SaleResponse.builder()
                .id(s.getId()).saleNumber(s.getSaleNumber())
                .customerId(s.getCustomer() != null ? s.getCustomer().getId() : null)
                .customerName(s.getCustomerName()).customerPhone(s.getCustomerPhone())
                .subtotal(s.getSubtotal()).taxAmount(s.getTaxAmount())
                .discountAmount(s.getDiscountAmount()).totalAmount(s.getTotalAmount())
                .paymentMethod(s.getPaymentMethod()).notes(s.getNotes())
                .status(s.getStatus())
                .soldById(s.getSoldBy().getId()).soldByName(s.getSoldBy().getFullName())
                .companyId(s.getCompany().getId()).saleDate(s.getSaleDate())
                .build();
    }

    private SaleItemResponse mapItemToResponse(SaleItem i) {
        return SaleItemResponse.builder()
                .id(i.getId()).productId(i.getProduct().getId())
                .productName(i.getProductName()).productSku(i.getProductSku())
                .quantity(i.getQuantity()).unitPrice(i.getUnitPrice()).costPrice(i.getCostPrice())
                .discount(i.getDiscount()).taxRate(i.getTaxRate()).taxAmount(i.getTaxAmount())
                .totalPrice(i.getTotalPrice()).build();
    }
}
