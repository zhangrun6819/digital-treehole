package com.compe.treehole.controller;

import com.compe.treehole.common.ApiResponse;
import com.compe.treehole.common.RequestContext;
import com.compe.treehole.dto.DoodleUploadResponse;
import com.compe.treehole.service.DoodleService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/doodles")
public class DoodleController {

    private final DoodleService doodleService;

    public DoodleController(DoodleService doodleService) {
        this.doodleService = doodleService;
    }

    @PostMapping
    public ApiResponse<DoodleUploadResponse> upload(@RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(doodleService.upload(RequestContext.visitorId(), file));
    }
}
