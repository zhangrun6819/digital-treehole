package com.compe.treehole.dto;

import com.compe.treehole.model.enums.RiskLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RiskRuleRequest(
        @NotBlank String keyword,
        @NotNull RiskLevel riskLevel,
        @NotNull Boolean enabled
) {
}
