package com.compe.treehole.dto;

import com.compe.treehole.model.enums.EmotionTag;
import com.compe.treehole.model.enums.InputType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
        @NotNull InputType inputType,
        @Size(max = 2000) String content,
        @Size(max = 500) String shortNote,
        EmotionTag emotionTag,
        Long doodleAssetId
) {
}
