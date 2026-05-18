package com.compe.treehole.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.compe.treehole.ai.AiChatRequest;
import com.compe.treehole.ai.AiChatResponse;
import com.compe.treehole.ai.AiCompanionClient;
import com.compe.treehole.common.AppException;
import com.compe.treehole.common.PageResponse;
import com.compe.treehole.config.TreeholeProperties;
import com.compe.treehole.dto.*;
import com.compe.treehole.mapper.ChatMessageMapper;
import com.compe.treehole.mapper.ChatSessionMapper;
import com.compe.treehole.model.entity.ChatMessage;
import com.compe.treehole.model.entity.ChatSession;
import com.compe.treehole.model.entity.DoodleAsset;
import com.compe.treehole.model.enums.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final AiCompanionClient aiCompanionClient;
    private final ModerationService moderationService;
    private final RiskGuardService riskGuardService;
    private final SupportResourceService supportResourceService;
    private final DoodleService doodleService;
    private final TreeholeProperties properties;

    public ChatService(
            ChatSessionMapper chatSessionMapper,
            ChatMessageMapper chatMessageMapper,
            AiCompanionClient aiCompanionClient,
            ModerationService moderationService,
            RiskGuardService riskGuardService,
            SupportResourceService supportResourceService,
            DoodleService doodleService,
            TreeholeProperties properties
    ) {
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.aiCompanionClient = aiCompanionClient;
        this.moderationService = moderationService;
        this.riskGuardService = riskGuardService;
        this.supportResourceService = supportResourceService;
        this.doodleService = doodleService;
        this.properties = properties;
    }

    public ChatSessionResponse createSession(Long visitorId, CreateChatSessionRequest request) {
        LocalDateTime now = LocalDateTime.now();
        ChatSession session = new ChatSession();
        session.setVisitorId(visitorId);
        session.setCompanionStyle(request.companionStyle().name());
        session.setTitle(request.title() == null || request.title().isBlank() ? "新的树洞对话" : request.title());
        session.setLatestRiskLevel(RiskLevel.NONE.name());
        session.setStatus("ACTIVE");
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        chatSessionMapper.insert(session);
        return toSessionResponse(session);
    }

    public PageResponse<ChatSessionResponse> listSessions(Long visitorId, long pageNo, long pageSize) {
        Page<ChatSession> page = chatSessionMapper.selectPage(Page.of(pageNo, pageSize), new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getVisitorId, visitorId)
                .orderByDesc(ChatSession::getUpdatedAt));
        return new PageResponse<>(page.getRecords().stream().map(this::toSessionResponse).toList(), page.getTotal(), pageNo, pageSize);
    }

    public ChatSessionResponse getSession(Long visitorId, Long sessionId) {
        return toSessionResponse(requireSession(visitorId, sessionId));
    }

    public PageResponse<ChatMessageResponse> listMessages(Long visitorId, Long sessionId, long pageNo, long pageSize) {
        requireSession(visitorId, sessionId);
        Page<ChatMessage> page = chatMessageMapper.selectPage(Page.of(pageNo, pageSize), new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .orderByAsc(ChatMessage::getCreatedAt));
        return new PageResponse<>(page.getRecords().stream().map(this::toMessageResponse).toList(), page.getTotal(), pageNo, pageSize);
    }

    @Transactional
    public SendMessageResponse sendMessage(Long visitorId, Long sessionId, SendMessageRequest request) {
        ChatSession session = requireSession(visitorId, sessionId);
        validateMessageRequest(request);

        DoodleAsset doodleAsset = null;
        if (request.inputType() == InputType.DOODLE) {
            doodleAsset = doodleService.requireUsableAsset(visitorId, request.doodleAssetId());
        }

        String textForCheck = request.inputType() == InputType.TEXT ? request.content() : request.shortNote();
        ModerationService.ModerationResult moderation = moderationService.moderate(textForCheck);
        RiskGuardService.RiskResult risk = riskGuardService.evaluate(textForCheck);

        LocalDateTime now = LocalDateTime.now();
        ChatMessage userMessage = new ChatMessage();
        userMessage.setSessionId(sessionId);
        userMessage.setVisitorId(visitorId);
        userMessage.setRole(MessageRole.USER.name());
        userMessage.setInputType(request.inputType().name());
        userMessage.setContent(request.inputType() == InputType.TEXT ? request.content() : null);
        userMessage.setShortNote(request.shortNote());
        userMessage.setSubmittedEmotionTag(request.emotionTag() == null ? null : request.emotionTag().name());
        userMessage.setDoodleAssetId(request.doodleAssetId());
        userMessage.setModerationStatus(moderation.status().name());
        userMessage.setRiskLevel(risk.level().name());
        userMessage.setProviderType(ProviderType.LOCAL.name());
        userMessage.setCreatedAt(now);
        userMessage.setUpdatedAt(now);
        chatMessageMapper.insert(userMessage);

        if (doodleAsset != null) {
            doodleService.bind(doodleAsset.getId(), userMessage.getId());
        }
        riskGuardService.recordEvent(visitorId, sessionId, userMessage.getId(), risk);

        AiChatResponse aiResponse = moderation.status() == ModerationStatus.BLOCKED
                ? blockedContentResponse()
                : callAi(session, request, doodleAsset, userMessage.getId());

        EmotionTag finalEmotion = EmotionTag.safeValueOf(aiResponse.emotionTag());
        userMessage.setEmotionTag(finalEmotion.name());
        userMessage.setUpdatedAt(LocalDateTime.now());
        chatMessageMapper.updateById(userMessage);

        ChatMessage assistantMessage = buildAssistantMessage(visitorId, sessionId, aiResponse, finalEmotion);
        chatMessageMapper.insert(assistantMessage);

        session.setLatestEmotionTag(finalEmotion.name());
        session.setLatestRiskLevel(risk.level().name());
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionMapper.updateById(session);

        List<SupportResourceResponse> resources = risk.level() == RiskLevel.HIGH || risk.level() == RiskLevel.MEDIUM
                ? supportResourceService.enabledResources()
                : List.of();
        return new SendMessageResponse(
                toMessageResponse(userMessage),
                toMessageResponse(assistantMessage),
                aiResponse.reframeText(),
                aiResponse.followUpQuestion(),
                risk.level().name(),
                resources
        );
    }

    private ChatMessage buildAssistantMessage(Long visitorId, Long sessionId, AiChatResponse aiResponse, EmotionTag finalEmotion) {
        LocalDateTime now = LocalDateTime.now();
        ChatMessage assistant = new ChatMessage();
        assistant.setSessionId(sessionId);
        assistant.setVisitorId(visitorId);
        assistant.setRole(MessageRole.ASSISTANT.name());
        assistant.setInputType(InputType.TEXT.name());
        assistant.setContent(aiResponse.comfortText() + "\n\n" + aiResponse.reframeText() + "\n\n" + aiResponse.followUpQuestion());
        assistant.setEmotionTag(finalEmotion.name());
        assistant.setModerationStatus(ModerationStatus.PASS.name());
        assistant.setRiskLevel(RiskLevel.NONE.name());
        assistant.setProviderType(aiResponse.providerType());
        assistant.setCreatedAt(now);
        assistant.setUpdatedAt(now);
        return assistant;
    }

    private AiChatResponse callAi(ChatSession session, SendMessageRequest request, DoodleAsset doodleAsset, Long currentMessageId) {
        try {
            List<AiChatRequest.HistoryMessage> history = recentHistory(session.getId(), currentMessageId);
            AiChatRequest.CurrentInput currentInput = new AiChatRequest.CurrentInput(
                    request.inputType().name(),
                    request.content(),
                    request.emotionTag() == null ? null : request.emotionTag().name(),
                    request.shortNote(),
                    doodleAsset == null ? null : doodleAsset.getPublicUrl()
            );
            return aiCompanionClient.chat(new AiChatRequest(session.getId(), session.getCompanionStyle(), history, currentInput));
        } catch (Exception ex) {
            return new AiChatResponse(
                    "我已经收到你的表达了。现在先不用急着把所有事情说清楚，我们可以从一个最小的感受开始。",
                    "转念一想，你愿意停下来照顾自己的感受，这已经是很重要的一步。",
                    request.emotionTag() == null ? EmotionTag.CALM.name() : request.emotionTag().name(),
                    RiskLevel.NONE.name(),
                    "此刻你最想被理解的是哪一部分？",
                    ProviderType.FALLBACK.name()
            );
        }
    }

    private List<AiChatRequest.HistoryMessage> recentHistory(Long sessionId, Long currentMessageId) {
        Page<ChatMessage> page = chatMessageMapper.selectPage(Page.of(1, properties.ai().maxHistoryMessages()), new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .lt(ChatMessage::getId, currentMessageId)
                .orderByDesc(ChatMessage::getCreatedAt));
        List<ChatMessage> reversed = new ArrayList<>(page.getRecords());
        java.util.Collections.reverse(reversed);
        return reversed.stream()
                .map(message -> new AiChatRequest.HistoryMessage(message.getRole(), message.getContent(), message.getEmotionTag()))
                .toList();
    }

    private AiChatResponse blockedContentResponse() {
        return new AiChatResponse(
                "这段内容里可能包含不适合直接发布的表达，我先帮你把情绪接住：你可以换一种更安全的方式说出来。",
                "转念一想，真正重要的不是攻击谁，而是看见自己为什么这么难受。",
                EmotionTag.ANGRY.name(),
                RiskLevel.NONE.name(),
                "如果把这句话改成描述自己的感受，你会怎么说？",
                ProviderType.LOCAL.name()
        );
    }

    private void validateMessageRequest(SendMessageRequest request) {
        if (request.inputType() == InputType.TEXT) {
            if (request.content() == null || request.content().isBlank()) {
                throw AppException.badRequest("文本消息不能为空");
            }
            return;
        }
        if (request.inputType() == InputType.DOODLE) {
            if (request.doodleAssetId() == null) {
                throw AppException.badRequest("涂鸦消息必须携带 doodleAssetId");
            }
            if (request.emotionTag() == null) {
                throw AppException.badRequest("涂鸦消息必须选择情绪标签");
            }
            return;
        }
        throw AppException.badRequest("不支持的输入类型");
    }

    private ChatSession requireSession(Long visitorId, Long sessionId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || !session.getVisitorId().equals(visitorId)) {
            throw AppException.notFound("会话不存在");
        }
        return session;
    }

    public ChatSessionResponse toSessionResponse(ChatSession session) {
        return new ChatSessionResponse(
                session.getId(),
                session.getCompanionStyle(),
                session.getTitle(),
                session.getLatestEmotionTag(),
                session.getLatestRiskLevel(),
                session.getStatus(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }

    public ChatMessageResponse toMessageResponse(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getSessionId(),
                message.getRole(),
                message.getInputType(),
                message.getContent(),
                message.getShortNote(),
                message.getEmotionTag(),
                message.getDoodleAssetId(),
                message.getModerationStatus(),
                message.getRiskLevel(),
                message.getProviderType(),
                message.getCreatedAt()
        );
    }
}
