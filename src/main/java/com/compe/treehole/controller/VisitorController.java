package com.compe.treehole.controller;

import com.compe.treehole.common.ApiResponse;
import com.compe.treehole.common.RequestContext;
import com.compe.treehole.dto.VisitorSessionResponse;
import com.compe.treehole.service.VisitorSessionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/visitors/session")
public class VisitorController {

    private final VisitorSessionService visitorSessionService;

    public VisitorController(VisitorSessionService visitorSessionService) {
        this.visitorSessionService = visitorSessionService;
    }

    @PostMapping
    public ApiResponse<VisitorSessionResponse> createSession() {
        return ApiResponse.ok(visitorSessionService.createSession());
    }

    @PostMapping("/refresh")
    public ApiResponse<VisitorSessionResponse> refreshSession() {
        return ApiResponse.ok(visitorSessionService.refresh(RequestContext.visitorId()));
    }
}
