package com.compe.treehole.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("risk_event")
public class RiskEvent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long visitorId;
    private Long sessionId;
    private Long messageId;
    private String riskLevel;
    private String matchedKeyword;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
