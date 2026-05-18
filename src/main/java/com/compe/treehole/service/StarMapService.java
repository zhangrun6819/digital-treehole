package com.compe.treehole.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.compe.treehole.dto.StarMapResponse;
import com.compe.treehole.mapper.ChatMessageMapper;
import com.compe.treehole.model.entity.ChatMessage;
import com.compe.treehole.model.enums.EmotionTag;
import com.compe.treehole.model.enums.MessageRole;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StarMapService {

    private final ChatMessageMapper chatMessageMapper;

    public StarMapService(ChatMessageMapper chatMessageMapper) {
        this.chatMessageMapper = chatMessageMapper;
    }

    public StarMapResponse weeklyStarMap(Long visitorId, int days) {
        int safeDays = Math.max(1, Math.min(days, 30));
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(safeDays - 1L);
        LocalDateTime startTime = start.atStartOfDay();
        List<ChatMessage> messages = chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getVisitorId, visitorId)
                .eq(ChatMessage::getRole, MessageRole.USER.name())
                .ge(ChatMessage::getCreatedAt, startTime)
                .isNotNull(ChatMessage::getEmotionTag)
                .orderByAsc(ChatMessage::getCreatedAt));
        if (messages.isEmpty()) {
            return new StarMapResponse(start, end, EmotionTag.CALM.name(), "最近还没有足够的心情记录，先留下第一颗星吧。", List.of());
        }

        Map<String, Long> counts = messages.stream()
                .collect(Collectors.groupingBy(ChatMessage::getEmotionTag, Collectors.counting()));
        String dominant = counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(EmotionTag.CALM.name());
        double total = messages.size();

        List<StarMapResponse.StarPoint> points = messages.stream()
                .map(message -> toPoint(message, counts.getOrDefault(message.getEmotionTag(), 1L), total))
                .sorted(Comparator.comparing(StarMapResponse.StarPoint::sourceDate))
                .toList();

        EmotionTag dominantTag = EmotionTag.safeValueOf(dominant);
        String summary = "最近 " + safeDays + " 天里，" + dominantTag.label() + "是出现最多的情绪。星星越大，代表这一类感受出现得越频繁。";
        return new StarMapResponse(start, end, dominantTag.name(), summary, points);
    }

    private StarMapResponse.StarPoint toPoint(ChatMessage message, long sameEmotionCount, double total) {
        EmotionTag tag = EmotionTag.safeValueOf(message.getEmotionTag());
        long hash = stableHash(message.getVisitorId() + ":" + message.getId() + ":" + safeText(message.getContent()) + ":" + safeText(message.getShortNote()));
        double x = hash % 10_000 / 100.0;
        double y = (hash / 10_000) % 10_000 / 100.0;
        int radius = 2 + (int) Math.min(3, sameEmotionCount);
        double intensity = sameEmotionCount / total;
        return new StarMapResponse.StarPoint(
                x,
                y,
                radius,
                tag.color(),
                tag.name(),
                message.getCreatedAt().toLocalDate(),
                Math.round(intensity * 100.0) / 100.0,
                tag.label()
        );
    }

    private long stableHash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            long value = 0L;
            for (int i = 0; i < 8; i++) {
                value = (value << 8) | (bytes[i] & 0xffL);
            }
            return Math.abs(value);
        } catch (Exception ex) {
            return Math.abs(text.hashCode());
        }
    }

    private String safeText(String text) {
        return text == null ? "" : text;
    }
}
