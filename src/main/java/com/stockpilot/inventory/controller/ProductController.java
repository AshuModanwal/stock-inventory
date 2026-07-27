package com.stockpilot.inventory.controller;

import com.stockpilot.inventory.dto.common.*;
import com.stockpilot.inventory.dto.product.*;
import com.stockpilot.inventory.security.CurrentUser;
import com.stockpilot.inventory.security.UserPrincipal;
import com.stockpilot.inventory.service.ProductService;
import com.stockpilot.inventory.util.ApiRoutes;
import com.stockpilot.inventory.util.Constants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiRoutes.PRODUCTS)
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @Valid @RequestBody ProductRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created", productService.create(request, user)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getAll(
            @CurrentUser UserPrincipal user,
            @RequestParam(defaultValue = Constants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = Constants.DEFAULT_SIZE) int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sortBy) {
        return ResponseEntity.ok(ApiResponse.success(productService.getAll(user.getCompanyId(), page, size, search, categoryId, sortBy)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(
            @PathVariable Long id, @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(productService.getById(id, user.getCompanyId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable Long id, @Valid @RequestBody ProductRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success("Product updated", productService.update(id, request, user)));
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleActive(@PathVariable Long id, @CurrentUser UserPrincipal user) {
        productService.toggleActive(id, user);
        return ResponseEntity.ok(ApiResponse.success("Product status toggled", null));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStock(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(productService.getLowStock(user.getCompanyId())));
    }
}
