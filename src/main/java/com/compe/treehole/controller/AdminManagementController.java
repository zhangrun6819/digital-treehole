package com.compe.treehole.controller;

import com.compe.treehole.common.ApiResponse;
import com.compe.treehole.common.RequestContext;
import com.compe.treehole.dto.RiskRuleRequest;
import com.compe.treehole.dto.RuleRequest;
import com.compe.treehole.dto.SupportResourceRequest;
import com.compe.treehole.model.entity.ContentRule;
import com.compe.treehole.model.entity.RiskEvent;
import com.compe.treehole.model.entity.RiskRule;
import com.compe.treehole.model.entity.SupportResource;
import com.compe.treehole.service.AdminManagementService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminManagementController {

    private final AdminManagementService adminManagementService;

    public AdminManagementController(AdminManagementService adminManagementService) {
        this.adminManagementService = adminManagementService;
    }

    @GetMapping("/risk-events")
    public ApiResponse<List<RiskEvent>> listRiskEvents() {
        RequestContext.adminId();
        return ApiResponse.ok(adminManagementService.listRiskEvents());
    }

    @GetMapping("/content-rules")
    public ApiResponse<List<ContentRule>> listContentRules() {
        RequestContext.adminId();
        return ApiResponse.ok(adminManagementService.listContentRules());
    }

    @PostMapping("/content-rules")
    public ApiResponse<ContentRule> createContentRule(@Valid @RequestBody RuleRequest request) {
        RequestContext.adminId();
        return ApiResponse.ok(adminManagementService.createContentRule(request));
    }

    @PutMapping("/content-rules/{id}")
    public ApiResponse<ContentRule> updateContentRule(@PathVariable Long id, @Valid @RequestBody RuleRequest request) {
        RequestContext.adminId();
        return ApiResponse.ok(adminManagementService.updateContentRule(id, request));
    }

    @GetMapping("/risk-rules")
    public ApiResponse<List<RiskRule>> listRiskRules() {
        RequestContext.adminId();
        return ApiResponse.ok(adminManagementService.listRiskRules());
    }

    @PostMapping("/risk-rules")
    public ApiResponse<RiskRule> createRiskRule(@Valid @RequestBody RiskRuleRequest request) {
        RequestContext.adminId();
        return ApiResponse.ok(adminManagementService.createRiskRule(request));
    }

    @PutMapping("/risk-rules/{id}")
    public ApiResponse<RiskRule> updateRiskRule(@PathVariable Long id, @Valid @RequestBody RiskRuleRequest request) {
        RequestContext.adminId();
        return ApiResponse.ok(adminManagementService.updateRiskRule(id, request));
    }

    @GetMapping("/support-resources")
    public ApiResponse<List<SupportResource>> listSupportResources() {
        RequestContext.adminId();
        return ApiResponse.ok(adminManagementService.listSupportResources());
    }

    @PostMapping("/support-resources")
    public ApiResponse<SupportResource> createSupportResource(@Valid @RequestBody SupportResourceRequest request) {
        RequestContext.adminId();
        return ApiResponse.ok(adminManagementService.createSupportResource(request));
    }

    @PutMapping("/support-resources/{id}")
    public ApiResponse<SupportResource> updateSupportResource(@PathVariable Long id, @Valid @RequestBody SupportResourceRequest request) {
        RequestContext.adminId();
        return ApiResponse.ok(adminManagementService.updateSupportResource(id, request));
    }
}
