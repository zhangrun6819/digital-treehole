package com.compe.treehole.ai;

import com.compe.treehole.config.TreeholeProperties;
import com.compe.treehole.model.enums.EmotionTag;
import com.compe.treehole.model.enums.ProviderType;
import com.compe.treehole.model.enums.RiskLevel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 阿里云百炼 / DashScope 的 OpenAI 兼容客户端。
 * 让大模型按固定 JSON 结构输出，再映射回 AiChatResponse。
 */
@Component
@ConditionalOnProperty(prefix = "treehole.ai", name = "provider", havingValue = "bailian")
public class BailianAiCompanionClient implements AiCompanionClient {

    private static final Logger log = LoggerFactory.getLogger(BailianAiCompanionClient.class);

    private static final String SYSTEM_PROMPT = """
            你是一名温柔、专业、负责任的中文心理陪伴助手，名字叫"小树"。
            用户是匿名访客，正在向"数字树洞"倾诉。请遵循以下规则：
            1. 使用温暖、口语化的中文，不说教、不诊断、不开药、不评判。
            2. 先共情、再陪伴、最后给一个温柔的开放式提问，避免命令式语气。
            3. 当识别到自伤、自杀、伤害他人或严重危机线索时，将 riskHint 标为 HIGH；
               明显抑郁/焦虑但无危险动作时标 MEDIUM；轻度负面情绪标 LOW；其余 NONE。
            4. 必须严格输出 JSON，不要带任何解释、Markdown 或代码块包装。

            JSON 字段：
            - comfortText: 一段共情回应（80-180 字）
            - reframeText: 一段轻柔的"转念一想…"式视角转换（40-120 字）
            - emotionTag: 从 [CALM, SAD, ANXIOUS, ANGRY, HOPEFUL] 中选一个最贴近用户当下的
            - riskHint: 从 [NONE, LOW, MEDIUM, HIGH] 中选一个
            - followUpQuestion: 一句开放式追问（20-60 字）
            """;

    private final TreeholeProperties properties;
    private final RestClient restClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public BailianAiCompanionClient(TreeholeProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        Duration timeout = Duration.ofSeconds(properties.ai().timeoutSeconds());
        factory.setConnectTimeout((int) timeout.toMillis());
        factory.setReadTimeout((int) timeout.toMillis());
        this.restClient = RestClient.builder()
                .baseUrl(properties.ai().baseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.ai().apiKey())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(factory)
                .build();
    }

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        ChatRequestBody body = buildRequestBody(request);
        try {
            ChatResponseBody response = restClient.post()
                    .uri("/chat/completions")
                    .body(body)
                    .retrieve()
                    .body(ChatResponseBody.class);
            if (response == null || response.choices == null || response.choices.isEmpty()) {
                throw new IllegalStateException("百炼返回空响应");
            }
            String content = response.choices.get(0).message.content;
            return parseModelOutput(content);
        } catch (Exception ex) {
            log.warn("调用百炼失败，降级到本地兜底回复: {}", ex.getMessage());
            return fallbackResponse(request);
        }
    }

    private ChatRequestBody buildRequestBody(AiChatRequest request) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", SYSTEM_PROMPT));

        if (request.historyMessages() != null) {
            for (AiChatRequest.HistoryMessage h : request.historyMessages()) {
                if (h.content() == null || h.content().isBlank()) {
                    continue;
                }
                String role = "ASSISTANT".equalsIgnoreCase(h.role()) ? "assistant" : "user";
                messages.add(new ChatMessage(role, h.content()));
            }
        }

        AiChatRequest.CurrentInput input = request.currentInput();
        StringBuilder userText = new StringBuilder();
        if (input.content() != null && !input.content().isBlank()) {
            userText.append(input.content());
        }
        if (input.shortNote() != null && !input.shortNote().isBlank()) {
            userText.append("\n[涂鸦旁注] ").append(input.shortNote());
        }
        if (input.emotionTag() != null && !input.emotionTag().isBlank()) {
            userText.append("\n[用户自报情绪] ").append(input.emotionTag());
        }
        if (input.doodleUrl() != null && !input.doodleUrl().isBlank()) {
            userText.append("\n[用户上传了一张涂鸦图片]");
        }
        messages.add(new ChatMessage("user", userText.toString().trim()));

        ChatRequestBody body = new ChatRequestBody();
        body.model = properties.ai().model();
        body.messages = messages;
        body.temperature = 0.7;
        body.responseFormat = new ResponseFormat("json_object");
        return body;
    }

    private AiChatResponse parseModelOutput(String json) throws JsonProcessingException {
        ModelOutput out = mapper.readValue(json, ModelOutput.class);
        EmotionTag tag = EmotionTag.safeValueOf(out.emotionTag);
        String risk = safeRisk(out.riskHint);
        return new AiChatResponse(
                blankFallback(out.comfortText, "我听到你了，不用一下子说清楚所有事，我陪着你。"),
                blankFallback(out.reframeText, "转念一想，愿意把心事说出来，已经是给自己的一份温柔。"),
                tag.name(),
                risk,
                blankFallback(out.followUpQuestion, "如果可以，再说一点点你现在的感觉好吗？"),
                ProviderType.HTTP.name()
        );
    }

    private AiChatResponse fallbackResponse(AiChatRequest request) {
        EmotionTag tag = EmotionTag.safeValueOf(request.currentInput().emotionTag());
        return new AiChatResponse(
                "我已经收到你的表达了。现在不用急着把所有事情说清楚，我们可以从一个最小的感受开始。",
                "转念一想，你愿意停下来照顾自己的感受，这已经是很重要的一步。",
                tag.name(),
                RiskLevel.NONE.name(),
                "此刻你最想被理解的是哪一部分？",
                ProviderType.FALLBACK.name()
        );
    }

    private static String safeRisk(String value) {
        if (value == null) return RiskLevel.NONE.name();
        try {
            return RiskLevel.valueOf(value.trim().toUpperCase()).name();
        } catch (IllegalArgumentException ignored) {
            return RiskLevel.NONE.name();
        }
    }

    private static String blankFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ChatRequestBody {
        public String model;
        public List<ChatMessage> messages;
        public Double temperature;
        @com.fasterxml.jackson.annotation.JsonProperty("response_format")
        public ResponseFormat responseFormat;
    }

    record ChatMessage(String role, String content) {
    }

    record ResponseFormat(String type) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ChatResponseBody {
        public List<Choice> choices;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Choice {
        public Message message;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Message {
        public String role;
        public String content;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ModelOutput {
        public String comfortText;
        public String reframeText;
        public String emotionTag;
        public String riskHint;
        public String followUpQuestion;
    }
}
