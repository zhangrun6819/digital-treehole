package com.compe.treehole.controller;

import com.compe.treehole.common.ApiResponse;
import com.compe.treehole.common.PageResponse;
import com.compe.treehole.common.RequestContext;
import com.compe.treehole.dto.*;
import com.compe.treehole.service.ChatService;
import com.compe.treehole.service.StarMapService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;
    private final StarMapService starMapService;

    public ChatController(ChatService chatService, StarMapService starMapService) {
        this.chatService = chatService;
        this.starMapService = starMapService;
    }

    @PostMapping("/sessions")
    public ApiResponse<ChatSessionResponse> createSession(@Valid @RequestBody CreateChatSessionRequest request) {
        return ApiResponse.ok(chatService.createSession(RequestContext.visitorId(), request));
    }

    @GetMapping("/sessions")
    public ApiResponse<PageResponse<ChatSessionResponse>> listSessions(
            @RequestParam(defaultValue = "1") @Min(1) long pageNo,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) long pageSize
    ) {
        return ApiResponse.ok(chatService.listSessions(RequestContext.visitorId(), pageNo, pageSize));
    }

    @GetMapping("/sessions/{sessionId}")
    public ApiResponse<ChatSessionResponse> getSession(@PathVariable Long sessionId) {
        return ApiResponse.ok(chatService.getSession(RequestContext.visitorId(), sessionId));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ApiResponse<PageResponse<ChatMessageResponse>> listMessages(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "1") @Min(1) long pageNo,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) long pageSize
    ) {
        return ApiResponse.ok(chatService.listMessages(RequestContext.visitorId(), sessionId, pageNo, pageSize));
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public ApiResponse<SendMessageResponse> sendMessage(@PathVariable Long sessionId, @Valid @RequestBody SendMessageRequest request) {
        return ApiResponse.ok(chatService.sendMessage(RequestContext.visitorId(), sessionId, request));
    }

    @GetMapping("/stats/star-map")
    public ApiResponse<StarMapResponse> starMap(@RequestParam(defaultValue = "7") @Min(1) @Max(30) int days) {
        return ApiResponse.ok(starMapService.weeklyStarMap(RequestContext.visitorId(), days));
    }
}
