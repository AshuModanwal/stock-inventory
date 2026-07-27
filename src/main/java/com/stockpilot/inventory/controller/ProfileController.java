package com.stockpilot.inventory.controller;

import com.stockpilot.inventory.dto.common.ApiResponse;
import com.stockpilot.inventory.dto.user.UserResponse;
import com.stockpilot.inventory.security.CurrentUser;
import com.stockpilot.inventory.security.UserPrincipal;
import com.stockpilot.inventory.service.AuthService;
import com.stockpilot.inventory.service.UserService;
import com.stockpilot.inventory.util.ApiRoutes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiRoutes.PROFILE)
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(user.getId())));
    }
}
