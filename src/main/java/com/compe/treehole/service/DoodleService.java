package com.compe.treehole.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.compe.treehole.common.AppException;
import com.compe.treehole.config.TreeholeProperties;
import com.compe.treehole.dto.DoodleUploadResponse;
import com.compe.treehole.mapper.DoodleAssetMapper;
import com.compe.treehole.model.entity.DoodleAsset;
import com.compe.treehole.model.enums.DoodleStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DoodleService {

    private final DoodleAssetMapper doodleAssetMapper;
    private final TreeholeProperties properties;

    public DoodleService(DoodleAssetMapper doodleAssetMapper, TreeholeProperties properties) {
        this.doodleAssetMapper = doodleAssetMapper;
        this.properties = properties;
    }

    public DoodleUploadResponse upload(Long visitorId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw AppException.badRequest("请选择 PNG 涂鸦图片");
        }
        if (!"image/png".equalsIgnoreCase(file.getContentType())) {
            throw AppException.badRequest("涂鸦只支持 PNG 格式");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw AppException.badRequest("涂鸦图片不能超过 2MB");
        }

        BufferedImage image = readImage(file);
        if (image.getWidth() > 1024 || image.getHeight() > 1024) {
            throw AppException.badRequest("涂鸦分辨率不能超过 1024x1024");
        }

        LocalDateTime now = LocalDateTime.now();
        String filename = UUID.randomUUID() + ".png";
        Path doodleDir = Path.of(properties.storage().doodleDir()).toAbsolutePath().normalize();
        Path target = doodleDir.resolve(filename);
        try {
            Files.createDirectories(doodleDir);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target);
            }
        } catch (IOException ex) {
            throw AppException.badRequest("涂鸦保存失败，请稍后重试");
        }

        DoodleAsset asset = new DoodleAsset();
        asset.setVisitorId(visitorId);
        asset.setStoragePath(target.toString());
        asset.setPublicUrl(properties.storage().publicBaseUrl() + "/assets/doodles/" + filename);
        asset.setOriginalFilename(file.getOriginalFilename());
        asset.setContentType(file.getContentType());
        asset.setFileSize(file.getSize());
        asset.setWidth(image.getWidth());
        asset.setHeight(image.getHeight());
        asset.setStatus(DoodleStatus.UPLOADED.name());
        asset.setCreatedAt(now);
        asset.setUpdatedAt(now);
        doodleAssetMapper.insert(asset);
        return toResponse(asset);
    }

    public DoodleAsset requireUsableAsset(Long visitorId, Long doodleAssetId) {
        DoodleAsset asset = doodleAssetMapper.selectById(doodleAssetId);
        if (asset == null || !asset.getVisitorId().equals(visitorId)) {
            throw AppException.notFound("涂鸦资源不存在");
        }
        if (DoodleStatus.DELETED.name().equals(asset.getStatus())) {
            throw AppException.badRequest("涂鸦资源已失效");
        }
        return asset;
    }

    public void bind(Long doodleAssetId, Long messageId) {
        DoodleAsset asset = doodleAssetMapper.selectById(doodleAssetId);
        if (asset == null) {
            return;
        }
        asset.setStatus(DoodleStatus.BOUND.name());
        asset.setBoundMessageId(messageId);
        asset.setUpdatedAt(LocalDateTime.now());
        doodleAssetMapper.updateById(asset);
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000L)
    public void cleanupOrphanDoodles() {
        LocalDateTime expiredAt = LocalDateTime.now().minusMinutes(properties.cleanup().orphanDoodleMinutes());
        doodleAssetMapper.selectList(new LambdaQueryWrapper<DoodleAsset>()
                        .eq(DoodleAsset::getStatus, DoodleStatus.UPLOADED.name())
                        .lt(DoodleAsset::getCreatedAt, expiredAt))
                .forEach(this::deleteAsset);
    }

    public DoodleUploadResponse toResponse(DoodleAsset asset) {
        return new DoodleUploadResponse(
                asset.getId(),
                asset.getPublicUrl(),
                asset.getFileSize(),
                asset.getWidth(),
                asset.getHeight(),
                asset.getStatus()
        );
    }

    private BufferedImage readImage(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw AppException.badRequest("涂鸦图片内容不是有效 PNG");
            }
            return image;
        } catch (IOException ex) {
            throw AppException.badRequest("涂鸦图片读取失败");
        }
    }

    private void deleteAsset(DoodleAsset asset) {
        try {
            Files.deleteIfExists(Path.of(asset.getStoragePath()));
        } catch (IOException ignored) {
            // Cleaning is best-effort; database status still prevents reuse.
        }
        asset.setStatus(DoodleStatus.DELETED.name());
        asset.setUpdatedAt(LocalDateTime.now());
        doodleAssetMapper.updateById(asset);
    }
}
