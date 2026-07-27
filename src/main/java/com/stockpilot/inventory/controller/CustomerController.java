package com.stockpilot.inventory.controller;

import com.stockpilot.inventory.dto.common.*;
import com.stockpilot.inventory.dto.customer.*;
import com.stockpilot.inventory.security.CurrentUser;
import com.stockpilot.inventory.security.UserPrincipal;
import com.stockpilot.inventory.service.CustomerService;
import com.stockpilot.inventory.util.ApiRoutes;
import com.stockpilot.inventory.util.Constants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiRoutes.CUSTOMERS)
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> create(
            @Valid @RequestBody CustomerRequest request, @CurrentUser UserPrincipal user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer created", customerService.create(request, user)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CustomerResponse>>> getAll(
            @CurrentUser UserPrincipal user,
            @RequestParam(defaultValue = Constants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = Constants.DEFAULT_SIZE) int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getAll(user.getCompanyId(), page, size, search)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getById(
            @PathVariable Long id, @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getById(id, user.getCompanyId())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(
            @PathVariable Long id, @Valid @RequestBody CustomerRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success("Customer updated", customerService.update(id, request, user)));
    }
}
