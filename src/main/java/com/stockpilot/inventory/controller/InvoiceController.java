package com.stockpilot.inventory.controller;

import com.stockpilot.inventory.dto.common.*;
import com.stockpilot.inventory.dto.invoice.InvoiceResponse;
import com.stockpilot.inventory.security.CurrentUser;
import com.stockpilot.inventory.security.UserPrincipal;
import com.stockpilot.inventory.service.InvoiceService;
import com.stockpilot.inventory.util.ApiRoutes;
import com.stockpilot.inventory.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiRoutes.INVOICES)
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<InvoiceResponse>>> getAll(
            @CurrentUser UserPrincipal user,
            @RequestParam(defaultValue = Constants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = Constants.DEFAULT_SIZE) int size) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getAll(user.getCompanyId(), page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getById(
            @PathVariable Long id, @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getById(id, user.getCompanyId())));
    }

    @PatchMapping("/{id}/mark-paid")
    public ResponseEntity<ApiResponse<InvoiceResponse>> markAsPaid(
            @PathVariable Long id, @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success("Invoice marked as paid", invoiceService.markAsPaid(id, user.getCompanyId())));
    }
}
