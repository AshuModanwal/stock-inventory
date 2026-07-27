package com.stockpilot.inventory.controller;

import com.stockpilot.inventory.dto.category.*;
import com.stockpilot.inventory.dto.common.*;
import com.stockpilot.inventory.security.CurrentUser;
import com.stockpilot.inventory.security.UserPrincipal;
import com.stockpilot.inventory.service.CategoryService;
import com.stockpilot.inventory.util.ApiRoutes;
import com.stockpilot.inventory.util.Constants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiRoutes.CATEGORIES)
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CategoryRequest request, @CurrentUser UserPrincipal user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created", categoryService.create(request, user)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CategoryResponse>>> getAll(
            @CurrentUser UserPrincipal user,
            @RequestParam(defaultValue = Constants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = Constants.DEFAULT_SIZE) int size) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAll(user.getCompanyId(), page, size)));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getActive(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getActiveList(user.getCompanyId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @PathVariable Long id, @Valid @RequestBody CategoryRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success("Category updated", categoryService.update(id, request, user)));
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleActive(@PathVariable Long id, @CurrentUser UserPrincipal user) {
        categoryService.toggleActive(id, user);
        return ResponseEntity.ok(ApiResponse.success("Category status toggled", null));
    }
}
