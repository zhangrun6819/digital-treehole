package com.compe.treehole.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SupportResourceRequest(
        @NotBlank String title,
        @NotBlank String contact,
        @Size(max = 500) String description,
        @NotNull Boolean enabled
) {
}
