package com.stockpilot.inventory.controller;

import com.stockpilot.inventory.dto.common.ApiResponse;
import com.stockpilot.inventory.dto.dashboard.DashboardResponse;
import com.stockpilot.inventory.security.CurrentUser;
import com.stockpilot.inventory.security.UserPrincipal;
import com.stockpilot.inventory.service.DashboardService;
import com.stockpilot.inventory.util.ApiRoutes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiRoutes.DASHBOARD)
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getDashboard(user.getCompanyId())));
    }
}
