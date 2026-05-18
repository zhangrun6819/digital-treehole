package com.compe.treehole.ai;

public record AiChatResponse(
        String comfortText,
        String reframeText,
        String emotionTag,
        String riskHint,
        String followUpQuestion,
        String providerType
) {
}
