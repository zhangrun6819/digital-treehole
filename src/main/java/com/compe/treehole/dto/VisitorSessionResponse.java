package com.compe.treehole.dto;

public record VisitorSessionResponse(
        Long visitorId,
        String visitorCode,
        String token,
        int expiresInDays
) {
}
