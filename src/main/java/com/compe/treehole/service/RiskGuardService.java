package com.compe.treehole.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.compe.treehole.mapper.RiskEventMapper;
import com.compe.treehole.mapper.RiskRuleMapper;
import com.compe.treehole.model.entity.RiskEvent;
import com.compe.treehole.model.entity.RiskRule;
import com.compe.treehole.model.enums.RiskLevel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class RiskGuardService {

    private final RiskRuleMapper riskRuleMapper;
    private final RiskEventMapper riskEventMapper;

    public RiskGuardService(RiskRuleMapper riskRuleMapper, RiskEventMapper riskEventMapper) {
        this.riskRuleMapper = riskRuleMapper;
        this.riskEventMapper = riskEventMapper;
    }

    public RiskResult evaluate(String text) {
        if (text == null || text.isBlank()) {
            return new RiskResult(RiskLevel.NONE, null);
        }
        List<RiskRule> matchedRules = riskRuleMapper.selectList(new LambdaQueryWrapper<RiskRule>()
                        .eq(RiskRule::getEnabled, true))
                .stream()
                .filter(rule -> text.contains(rule.getKeyword()))
                .toList();
        return matchedRules.stream()
                .max(Comparator.comparing(rule -> RiskLevel.valueOf(rule.getRiskLevel()).ordinal()))
                .map(rule -> new RiskResult(RiskLevel.valueOf(rule.getRiskLevel()), rule.getKeyword()))
                .orElse(new RiskResult(RiskLevel.NONE, null));
    }

    public void recordEvent(Long visitorId, Long sessionId, Long messageId, RiskResult result) {
        if (result.level() != RiskLevel.HIGH && result.level() != RiskLevel.MEDIUM) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        RiskEvent event = new RiskEvent();
        event.setVisitorId(visitorId);
        event.setSessionId(sessionId);
        event.setMessageId(messageId);
        event.setRiskLevel(result.level().name());
        event.setMatchedKeyword(result.matchedKeyword());
        event.setStatus("OPEN");
        event.setCreatedAt(now);
        event.setUpdatedAt(now);
        riskEventMapper.insert(event);
    }

    public record RiskResult(RiskLevel level, String matchedKeyword) {
    }
}
