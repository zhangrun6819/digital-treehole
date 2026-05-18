package com.compe.treehole.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("doodle_asset")
public class DoodleAsset {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long visitorId;
    private String storagePath;
    private String publicUrl;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private Integer width;
    private Integer height;
    private String status;
    private Long boundMessageId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
