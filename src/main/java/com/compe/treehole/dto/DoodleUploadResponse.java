package com.compe.treehole.dto;

public record DoodleUploadResponse(
        Long doodleAssetId,
        String publicUrl,
        Long fileSize,
        Integer width,
        Integer height,
        String status
) {
}
