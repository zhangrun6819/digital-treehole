package com.compe.treehole.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.compe.treehole.mapper.ContentRuleMapper;
import com.compe.treehole.model.entity.ContentRule;
import com.compe.treehole.model.enums.ModerationStatus;
import com.compe.treehole.model.enums.RuleAction;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModerationService {

    private final ContentRuleMapper contentRuleMapper;

    public ModerationService(ContentRuleMapper contentRuleMapper) {
        this.contentRuleMapper = contentRuleMapper;
    }

    public ModerationResult moderate(String text) {
        if (text == null || text.isBlank()) {
            return new ModerationResult(ModerationStatus.PASS, null);
        }
        List<ContentRule> rules = contentRuleMapper.selectList(new LambdaQueryWrapper<ContentRule>()
                .eq(ContentRule::getEnabled, true));
        ModerationResult marked = null;
        for (ContentRule rule : rules) {
            if (text.contains(rule.getKeyword())) {
                if (RuleAction.BLOCK.name().equals(rule.getAction())) {
                    return new ModerationResult(ModerationStatus.BLOCKED, rule.getKeyword());
                }
                marked = new ModerationResult(ModerationStatus.MARKED, rule.getKeyword());
            }
        }
        return marked == null ? new ModerationResult(ModerationStatus.PASS, null) : marked;
    }

    public record ModerationResult(ModerationStatus status, String matchedKeyword) {
    }
}
