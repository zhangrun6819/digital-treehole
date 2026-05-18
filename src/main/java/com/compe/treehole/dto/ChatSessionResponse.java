package com.compe.treehole.dto;

import java.time.LocalDateTime;

public record ChatSessionResponse(
        Long id,
        String companionStyle,
        String title,
        String latestEmotionTag,
        String latestRiskLevel,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
