package com.compe.treehole.ai;

import com.compe.treehole.model.enums.EmotionTag;
import com.compe.treehole.model.enums.ProviderType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "treehole.ai", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockAiCompanionClient implements AiCompanionClient {

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        EmotionTag tag = inferEmotion(request.currentInput().content(), request.currentInput().emotionTag());
        String comfort = switch (tag) {
            case SAD -> "我听见你现在有点难过。先不用急着证明自己很坚强，能把这件事说出来，本身就是在照顾自己。";
            case ANXIOUS -> "这种焦虑感很真实，也很累。我们先把它拆小一点：此刻最需要处理的，可能只是下一步，而不是全部未来。";
            case ANGRY -> "生气说明你在意边界和公平。先让情绪落地，再决定怎么表达，会更容易保护自己。";
            case HOPEFUL -> "你已经在往亮一点的地方看了。哪怕只是一个很小的念头，也值得被认真接住。";
            case CALM -> "现在的平静很珍贵。可以把它当成一个小小的停靠点，慢慢整理今天的感受。";
        };
        String reframe = switch (tag) {
            case SAD -> "转念一想，这段低落不是退步，而是在提醒你：你需要被支持，也值得被支持。";
            case ANXIOUS -> "转念一想，焦虑不是说明你不行，而是说明这件事对你重要，我们可以先从可控的一小步开始。";
            case ANGRY -> "转念一想，愤怒背后也许藏着你真正重视的东西，找到它，会比压住情绪更有力量。";
            case HOPEFUL -> "转念一想，这份期待已经是你给自己的光，可以先守住它，再慢慢往前走。";
            case CALM -> "转念一想，平静不是没有情绪，而是你正在和自己好好相处。";
        };
        return new AiChatResponse(
                comfort,
                reframe,
                tag.name(),
                "NONE",
                "如果愿意的话，可以再说一点：这件事里最让你放不下的部分是什么？",
                ProviderType.MOCK.name()
        );
    }

    private EmotionTag inferEmotion(String content, String submittedEmotion) {
        if (submittedEmotion != null && !submittedEmotion.isBlank()) {
            return EmotionTag.safeValueOf(submittedEmotion);
        }
        if (content == null) {
            return EmotionTag.CALM;
        }
        if (content.contains("焦虑") || content.contains("紧张") || content.contains("害怕")) {
            return EmotionTag.ANXIOUS;
        }
        if (content.contains("难过") || content.contains("伤心") || content.contains("崩溃")) {
            return EmotionTag.SAD;
        }
        if (content.contains("生气") || content.contains("愤怒")) {
            return EmotionTag.ANGRY;
        }
        if (content.contains("希望") || content.contains("期待")) {
            return EmotionTag.HOPEFUL;
        }
        return EmotionTag.CALM;
    }
}
