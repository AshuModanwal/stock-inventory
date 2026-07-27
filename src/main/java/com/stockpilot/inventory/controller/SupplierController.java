package com.stockpilot.inventory.controller;

import com.stockpilot.inventory.dto.common.*;
import com.stockpilot.inventory.dto.supplier.*;
import com.stockpilot.inventory.security.CurrentUser;
import com.stockpilot.inventory.security.UserPrincipal;
import com.stockpilot.inventory.service.SupplierService;
import com.stockpilot.inventory.util.ApiRoutes;
import com.stockpilot.inventory.util.Constants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiRoutes.SUPPLIERS)
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<SupplierResponse>> create(
            @Valid @RequestBody SupplierRequest request, @CurrentUser UserPrincipal user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Supplier created", supplierService.create(request, user)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<SupplierResponse>>> getAll(
            @CurrentUser UserPrincipal user,
            @RequestParam(defaultValue = Constants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = Constants.DEFAULT_SIZE) int size) {
        return ResponseEntity.ok(ApiResponse.success(supplierService.getAll(user.getCompanyId(), page, size)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<SupplierResponse>> update(
            @PathVariable Long id, @Valid @RequestBody SupplierRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success("Supplier updated", supplierService.update(id, request, user)));
    }
}
