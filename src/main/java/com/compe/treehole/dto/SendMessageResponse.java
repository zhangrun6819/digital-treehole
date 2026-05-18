package com.compe.treehole.dto;

import java.util.List;

public record SendMessageResponse(
        ChatMessageResponse userMessage,
        ChatMessageResponse assistantMessage,
        String reframeText,
        String followUpQuestion,
        String riskLevel,
        List<SupportResourceResponse> supportResources
) {
}
