package com.compe.treehole.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private Long visitorId;
    private String role;
    private String inputType;
    private String content;
    private String shortNote;
    private String emotionTag;
    private String submittedEmotionTag;
    private Long doodleAssetId;
    private String moderationStatus;
    private String riskLevel;
    private String providerType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
