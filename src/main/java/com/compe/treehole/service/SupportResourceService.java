package com.compe.treehole.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.compe.treehole.dto.SupportResourceResponse;
import com.compe.treehole.mapper.SupportResourceMapper;
import com.compe.treehole.model.entity.SupportResource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupportResourceService {

    private final SupportResourceMapper supportResourceMapper;

    public SupportResourceService(SupportResourceMapper supportResourceMapper) {
        this.supportResourceMapper = supportResourceMapper;
    }

    public List<SupportResourceResponse> enabledResources() {
        return supportResourceMapper.selectList(new LambdaQueryWrapper<SupportResource>()
                        .eq(SupportResource::getEnabled, true)
                        .orderByAsc(SupportResource::getId))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SupportResourceResponse toResponse(SupportResource resource) {
        return new SupportResourceResponse(resource.getId(), resource.getTitle(), resource.getContact(), resource.getDescription());
    }
}
