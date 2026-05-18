package com.compe.treehole.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.compe.treehole.auth.HashService;
import com.compe.treehole.auth.JwtService;
import com.compe.treehole.common.AppException;
import com.compe.treehole.config.TreeholeProperties;
import com.compe.treehole.dto.AdminLoginRequest;
import com.compe.treehole.dto.AdminLoginResponse;
import com.compe.treehole.mapper.AdminUserMapper;
import com.compe.treehole.model.entity.AdminUser;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final AdminUserMapper adminUserMapper;
    private final HashService hashService;
    private final JwtService jwtService;
    private final TreeholeProperties properties;

    public AdminService(AdminUserMapper adminUserMapper, HashService hashService, JwtService jwtService, TreeholeProperties properties) {
        this.adminUserMapper = adminUserMapper;
        this.hashService = hashService;
        this.jwtService = jwtService;
        this.properties = properties;
    }

    public AdminLoginResponse login(AdminLoginRequest request) {
        AdminUser admin = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, request.username())
                .eq(AdminUser::getEnabled, true));
        if (admin == null || !admin.getPasswordHash().equals(hashService.sha256(request.password()))) {
            throw AppException.unauthorized("管理员账号或密码错误");
        }
        String token = jwtService.issueToken(admin.getId(), "ADMIN", admin.getUsername());
        return new AdminLoginResponse(admin.getId(), admin.getUsername(), admin.getDisplayName(), token, properties.auth().tokenDays());
    }
}
