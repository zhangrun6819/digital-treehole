package com.compe.treehole.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_session")
public class ChatSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long visitorId;
    private String companionStyle;
    private String title;
    private String latestEmotionTag;
    private String latestRiskLevel;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
