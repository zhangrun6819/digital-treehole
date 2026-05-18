package com.compe.treehole.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.compe.treehole.model.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
