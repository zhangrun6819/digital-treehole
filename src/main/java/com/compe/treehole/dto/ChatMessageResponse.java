package com.compe.treehole.dto;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        Long sessionId,
        String role,
        String inputType,
        String content,
        String shortNote,
        String emotionTag,
        Long doodleAssetId,
        String moderationStatus,
        String riskLevel,
        String providerType,
        LocalDateTime createdAt
) {
}
