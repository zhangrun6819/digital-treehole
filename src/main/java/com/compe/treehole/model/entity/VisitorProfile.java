package com.compe.treehole.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("visitor_profile")
public class VisitorProfile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String visitorCode;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
