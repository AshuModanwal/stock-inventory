package com.stockpilot.inventory.service;

import com.stockpilot.inventory.dto.common.PagedResponse;
import com.stockpilot.inventory.dto.purchase.*;
import com.stockpilot.inventory.entity.*;
import com.stockpilot.inventory.enums.*;
import com.stockpilot.inventory.exception.*;
import com.stockpilot.inventory.repository.*;
import com.stockpilot.inventory.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository poRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final StockMovementRepository movementRepository;
    private final UserRepository userRepository;

    @Transactional
    public PurchaseOrderResponse create(PurchaseOrderRequest request, UserPrincipal currentUser) {
        Company company = companyRepository.findById(currentUser.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", currentUser.getCompanyId()));
        Supplier supplier = supplierRepository.findByIdAndCompanyId(request.getSupplierId(), company.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", request.getSupplierId()));

        String poNumber = "PO-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%05d", poRepository.countByCompanyId(company.getId()) + 1);

        double subtotal = 0;
        List<PurchaseOrderItem> items = new ArrayList<>();

        for (PurchaseOrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findByIdAndCompanyId(itemReq.getProductId(), company.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemReq.getProductId()));
            double totalPrice = itemReq.getQuantity() * itemReq.getUnitPrice();
            subtotal += totalPrice;

            items.add(PurchaseOrderItem.builder()
                    .product(product).quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice()).totalPrice(totalPrice).build());
        }

        double taxAmount = subtotal * (company.getTaxRate() / 100.0);

        PurchaseOrder po = PurchaseOrder.builder()
                .poNumber(poNumber).supplier(supplier).subtotal(subtotal)
                .taxAmount(taxAmount).totalAmount(subtotal + taxAmount)
                .expectedDate(request.getExpectedDate()).notes(request.getNotes())
                .company(company).status(PurchaseOrderStatus.DRAFT).build();

        po = poRepository.save(po);
        for (PurchaseOrderItem item : items) {
            item.setPurchaseOrder(po);
        }
        po.setItems(items);
        po = poRepository.save(po);

        return mapToResponse(po);
    }

    @Transactional
    public PurchaseOrderResponse receive(Long id, UserPrincipal currentUser) {
        PurchaseOrder po = poRepository.findByIdAndCompanyId(id, currentUser.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        po.setStatus(PurchaseOrderStatus.RECEIVED);
        po.setReceivedDate(LocalDate.now());

        // Add stock for each item
        for (PurchaseOrderItem item : po.getItems()) {
            Product product = item.getProduct();
            int prevQty = product.getQuantity();
            product.setQuantity(prevQty + item.getQuantity());
            product.setCostPrice(item.getUnitPrice());
            productRepository.save(product);

            item.setReceivedQty(item.getQuantity());

            movementRepository.save(StockMovement.builder()
                    .product(product).type(StockMovementType.PURCHASE)
                    .quantity(item.getQuantity()).previousQty(prevQty)
                    .newQty(product.getQuantity()).unitPrice(item.getUnitPrice())
                    .referenceId(po.getPoNumber()).performedBy(user)
                    .company(po.getCompany()).build());
        }

        return mapToResponse(poRepository.save(po));
    }

    @Transactional(readOnly = true)
    public PagedResponse<PurchaseOrderResponse> getAll(Long companyId, int page, int size) {
        Page<PurchaseOrder> pos = poRepository.findByCompanyId(companyId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PagedResponse.<PurchaseOrderResponse>builder()
                .content(pos.getContent().stream().map(this::mapToResponse).toList())
                .page(pos.getNumber()).size(pos.getSize())
                .totalElements(pos.getTotalElements()).totalPages(pos.getTotalPages())
                .last(pos.isLast()).build();
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse getById(Long id, Long companyId) {
        return mapToResponse(poRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id)));
    }

    private PurchaseOrderResponse mapToResponse(PurchaseOrder po) {
        return PurchaseOrderResponse.builder()
                .id(po.getId()).poNumber(po.getPoNumber())
                .supplierId(po.getSupplier().getId()).supplierName(po.getSupplier().getName())
                .items(po.getItems().stream().map(i -> PurchaseOrderItemResponse.builder()
                        .id(i.getId()).productId(i.getProduct().getId())
                        .productName(i.getProduct().getName()).quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice()).receivedQty(i.getReceivedQty())
                        .totalPrice(i.getTotalPrice()).build()).toList())
                .subtotal(po.getSubtotal()).taxAmount(po.getTaxAmount())
                .totalAmount(po.getTotalAmount()).status(po.getStatus())
                .expectedDate(po.getExpectedDate()).receivedDate(po.getReceivedDate())
                .notes(po.getNotes()).companyId(po.getCompany().getId())
                .createdAt(po.getCreatedAt()).build();
    }
}
