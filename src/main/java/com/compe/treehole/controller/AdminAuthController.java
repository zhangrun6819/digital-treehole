package com.compe.treehole.controller;

import com.compe.treehole.common.ApiResponse;
import com.compe.treehole.dto.AdminLoginRequest;
import com.compe.treehole.dto.AdminLoginResponse;
import com.compe.treehole.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/auth")
public class AdminAuthController {

    private final AdminService adminService;

    public AdminAuthController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.ok(adminService.login(request));
    }
}
