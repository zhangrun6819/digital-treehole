package com.compe.treehole.auth;

import com.compe.treehole.common.AppException;
import com.compe.treehole.common.RequestContext;
import com.compe.treehole.config.TreeholeProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private final TreeholeProperties properties;
    private final SecretKey secretKey;

    public JwtService(TreeholeProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.auth().jwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String issueToken(Long userId, String role, String subject) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.auth().tokenDays(), ChronoUnit.DAYS);
        return Jwts.builder()
                .subject(subject)
                .claim("uid", userId)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public RequestContext.AuthUser parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Long userId = ((Number) claims.get("uid")).longValue();
            String role = String.valueOf(claims.get("role"));
            return new RequestContext.AuthUser(userId, role, claims.getSubject());
        } catch (Exception ex) {
            throw AppException.unauthorized("登录状态已失效，请重新进入");
        }
    }

    public boolean shouldRefreshSoon(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Instant expiresAt = claims.getExpiration().toInstant();
        Instant threshold = Instant.now().plus(properties.auth().refreshThresholdHours(), ChronoUnit.HOURS);
        return expiresAt.isBefore(threshold);
    }
}
