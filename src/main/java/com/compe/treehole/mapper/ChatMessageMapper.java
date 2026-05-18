package com.compe.treehole.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.compe.treehole.model.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
