package com.compe.treehole.service;

import com.compe.treehole.auth.JwtService;
import com.compe.treehole.common.AppException;
import com.compe.treehole.config.TreeholeProperties;
import com.compe.treehole.dto.VisitorSessionResponse;
import com.compe.treehole.mapper.VisitorProfileMapper;
import com.compe.treehole.model.entity.VisitorProfile;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class VisitorSessionService {

    private final VisitorProfileMapper visitorProfileMapper;
    private final JwtService jwtService;
    private final TreeholeProperties properties;
    private final HttpServletRequest httpRequest;

    public VisitorSessionService(VisitorProfileMapper visitorProfileMapper, JwtService jwtService, TreeholeProperties properties, HttpServletRequest httpRequest) {
        this.visitorProfileMapper = visitorProfileMapper;
        this.jwtService = jwtService;
        this.properties = properties;
        this.httpRequest = httpRequest;
    }

    public VisitorSessionResponse createSession() {
        LocalDateTime now = LocalDateTime.now();
        VisitorProfile visitor = new VisitorProfile();
        visitor.setVisitorCode(UUID.randomUUID().toString());
        visitor.setStatus("ACTIVE");
        visitor.setCreatedAt(now);
        visitor.setUpdatedAt(now);
        visitorProfileMapper.insert(visitor);
        return buildResponse(visitor);
    }

    public VisitorSessionResponse refresh(Long visitorId) {
        String authorization = httpRequest.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw AppException.unauthorized("缺少 Authorization Bearer token");
        }
        if (!jwtService.shouldRefreshSoon(authorization.substring(7))) {
            throw AppException.badRequest("当前 token 还在有效期内，无需续期");
        }
        VisitorProfile visitor = visitorProfileMapper.selectById(visitorId);
        if (visitor == null) {
            return createSession();
        }
        visitor.setUpdatedAt(LocalDateTime.now());
        visitorProfileMapper.updateById(visitor);
        return buildResponse(visitor);
    }

    private VisitorSessionResponse buildResponse(VisitorProfile visitor) {
        String token = jwtService.issueToken(visitor.getId(), "VISITOR", visitor.getVisitorCode());
        return new VisitorSessionResponse(visitor.getId(), visitor.getVisitorCode(), token, properties.auth().tokenDays());
    }
}
