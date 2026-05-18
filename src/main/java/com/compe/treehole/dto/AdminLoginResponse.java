package com.compe.treehole.dto;

public record AdminLoginResponse(
        Long adminId,
        String username,
        String displayName,
        String token,
        int expiresInDays
) {
}
