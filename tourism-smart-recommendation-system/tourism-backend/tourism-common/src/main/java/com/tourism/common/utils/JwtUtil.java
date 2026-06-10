package com.tourism.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT工具类
 *
 */
@Component
public class JwtUtil {

    /**
     * 密钥（实际项目中应配置在配置文件中）
     */
    private static final String SECRET = "tourism-smart-recommendation-system-jwt-secret-key-2025";

    /**
     * Token有效期：7天
     */
    private static final long EXPIRATION = 7 * 24 * 60 * 60 * 1000;

    /**
     * 生成密钥
     */
    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成Token
     *
     * @param subject    主题（用户ID）
     * @param claims     自定义声明
     * @return Token
     */
    public String generateToken(String subject, Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION);

        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSignKey())
                .compact();
    }

    /**
     * 解析Token
     *
     * @param token Token
     * @return Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 获取用户ID
     *
     * @param token Token
     * @return 用户ID
     */
    public String getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 验证Token是否有效
     *
     * @param token Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取Token过期时间
     *
     * @param token Token
     * @return 过期时间
     */
    public Date getExpiration(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }

    /**
     * 创建用户Token
     *
     * @param userId 用户ID
     * @param role   用户角色
     * @return Token
     */
    public String createToken(Long userId, String role) {
        return generateToken(userId.toString(), Map.of("role", role));
    }

    /**
     * 从Token获取用户ID
     *
     * @param token Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从Token获取用户角色
     *
     * @param token Token
     * @return 用户角色
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }
}
