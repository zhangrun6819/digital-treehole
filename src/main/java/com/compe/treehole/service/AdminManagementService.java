package com.compe.treehole.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.compe.treehole.common.AppException;
import com.compe.treehole.dto.RiskRuleRequest;
import com.compe.treehole.dto.RuleRequest;
import com.compe.treehole.dto.SupportResourceRequest;
import com.compe.treehole.mapper.ContentRuleMapper;
import com.compe.treehole.mapper.RiskEventMapper;
import com.compe.treehole.mapper.RiskRuleMapper;
import com.compe.treehole.mapper.SupportResourceMapper;
import com.compe.treehole.model.entity.ContentRule;
import com.compe.treehole.model.entity.RiskEvent;
import com.compe.treehole.model.entity.RiskRule;
import com.compe.treehole.model.entity.SupportResource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminManagementService {

    private final RiskEventMapper riskEventMapper;
    private final ContentRuleMapper contentRuleMapper;
    private final RiskRuleMapper riskRuleMapper;
    private final SupportResourceMapper supportResourceMapper;

    public AdminManagementService(
            RiskEventMapper riskEventMapper,
            ContentRuleMapper contentRuleMapper,
            RiskRuleMapper riskRuleMapper,
            SupportResourceMapper supportResourceMapper
    ) {
        this.riskEventMapper = riskEventMapper;
        this.contentRuleMapper = contentRuleMapper;
        this.riskRuleMapper = riskRuleMapper;
        this.supportResourceMapper = supportResourceMapper;
    }

    public List<RiskEvent> listRiskEvents() {
        return riskEventMapper.selectList(new LambdaQueryWrapper<RiskEvent>().orderByDesc(RiskEvent::getCreatedAt));
    }

    public List<ContentRule> listContentRules() {
        return contentRuleMapper.selectList(new LambdaQueryWrapper<ContentRule>().orderByAsc(ContentRule::getId));
    }

    public ContentRule createContentRule(RuleRequest request) {
        LocalDateTime now = LocalDateTime.now();
        ContentRule rule = new ContentRule();
        rule.setKeyword(request.keyword());
        rule.setCategory(request.category());
        rule.setAction(request.action());
        rule.setEnabled(request.enabled());
        rule.setCreatedAt(now);
        rule.setUpdatedAt(now);
        contentRuleMapper.insert(rule);
        return rule;
    }

    public ContentRule updateContentRule(Long id, RuleRequest request) {
        ContentRule rule = contentRuleMapper.selectById(id);
        if (rule == null) {
            throw AppException.notFound("内容审核规则不存在");
        }
        rule.setKeyword(request.keyword());
        rule.setCategory(request.category());
        rule.setAction(request.action());
        rule.setEnabled(request.enabled());
        rule.setUpdatedAt(LocalDateTime.now());
        contentRuleMapper.updateById(rule);
        return rule;
    }

    public List<RiskRule> listRiskRules() {
        return riskRuleMapper.selectList(new LambdaQueryWrapper<RiskRule>().orderByAsc(RiskRule::getId));
    }

    public RiskRule createRiskRule(RiskRuleRequest request) {
        LocalDateTime now = LocalDateTime.now();
        RiskRule rule = new RiskRule();
        rule.setKeyword(request.keyword());
        rule.setRiskLevel(request.riskLevel().name());
        rule.setEnabled(request.enabled());
        rule.setCreatedAt(now);
        rule.setUpdatedAt(now);
        riskRuleMapper.insert(rule);
        return rule;
    }

    public RiskRule updateRiskRule(Long id, RiskRuleRequest request) {
        RiskRule rule = riskRuleMapper.selectById(id);
        if (rule == null) {
            throw AppException.notFound("风险规则不存在");
        }
        rule.setKeyword(request.keyword());
        rule.setRiskLevel(request.riskLevel().name());
        rule.setEnabled(request.enabled());
        rule.setUpdatedAt(LocalDateTime.now());
        riskRuleMapper.updateById(rule);
        return rule;
    }

    public List<SupportResource> listSupportResources() {
        return supportResourceMapper.selectList(new LambdaQueryWrapper<SupportResource>().orderByAsc(SupportResource::getId));
    }

    public SupportResource createSupportResource(SupportResourceRequest request) {
        LocalDateTime now = LocalDateTime.now();
        SupportResource resource = new SupportResource();
        resource.setTitle(request.title());
        resource.setContact(request.contact());
        resource.setDescription(request.description());
        resource.setEnabled(request.enabled());
        resource.setCreatedAt(now);
        resource.setUpdatedAt(now);
        supportResourceMapper.insert(resource);
        return resource;
    }

    public SupportResource updateSupportResource(Long id, SupportResourceRequest request) {
        SupportResource resource = supportResourceMapper.selectById(id);
        if (resource == null) {
            throw AppException.notFound("心理援助资源不存在");
        }
        resource.setTitle(request.title());
        resource.setContact(request.contact());
        resource.setDescription(request.description());
        resource.setEnabled(request.enabled());
        resource.setUpdatedAt(LocalDateTime.now());
        supportResourceMapper.updateById(resource);
        return resource;
    }
}
