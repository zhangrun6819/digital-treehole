package com.compe.treehole.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RuleRequest(
        @NotBlank String keyword,
        @NotBlank String category,
        @NotBlank String action,
        @NotNull Boolean enabled
) {
}
