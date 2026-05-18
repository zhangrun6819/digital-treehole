package com.compe.treehole.dto;

import com.compe.treehole.model.enums.CompanionStyle;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateChatSessionRequest(
        @NotNull CompanionStyle companionStyle,
        @Size(max = 120) String title
) {
}
