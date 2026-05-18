package com.compe.treehole.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.compe.treehole.model.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
}
