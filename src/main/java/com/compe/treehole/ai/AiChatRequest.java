package com.compe.treehole.ai;

import java.util.List;

public record AiChatRequest(
        Long sessionId,
        String companionStyle,
        List<HistoryMessage> historyMessages,
        CurrentInput currentInput
) {
    public record HistoryMessage(String role, String content, String emotionTag) {
    }

    public record CurrentInput(
            String inputType,
            String content,
            String emotionTag,
            String shortNote,
            String doodleUrl
    ) {
    }
}
