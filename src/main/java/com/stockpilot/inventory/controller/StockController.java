package com.stockpilot.inventory.controller;

import com.stockpilot.inventory.dto.common.*;
import com.stockpilot.inventory.dto.stock.*;
import com.stockpilot.inventory.security.CurrentUser;
import com.stockpilot.inventory.security.UserPrincipal;
import com.stockpilot.inventory.service.StockService;
import com.stockpilot.inventory.util.ApiRoutes;
import com.stockpilot.inventory.util.Constants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiRoutes.STOCK)
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping("/adjust")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<ApiResponse<StockMovementResponse>> adjust(
            @Valid @RequestBody StockAdjustmentRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success("Stock adjusted", stockService.adjustStock(request, user)));
    }

    @GetMapping("/movements")
    public ResponseEntity<ApiResponse<PagedResponse<StockMovementResponse>>> getMovements(
            @CurrentUser UserPrincipal user,
            @RequestParam(defaultValue = Constants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = Constants.DEFAULT_SIZE) int size) {
        return ResponseEntity.ok(ApiResponse.success(stockService.getMovements(user.getCompanyId(), page, size)));
    }

    @GetMapping("/movements/product/{productId}")
    public ResponseEntity<ApiResponse<PagedResponse<StockMovementResponse>>> getProductMovements(
            @PathVariable Long productId,
            @RequestParam(defaultValue = Constants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = Constants.DEFAULT_SIZE) int size) {
        return ResponseEntity.ok(ApiResponse.success(stockService.getProductMovements(productId, page, size)));
    }
}
