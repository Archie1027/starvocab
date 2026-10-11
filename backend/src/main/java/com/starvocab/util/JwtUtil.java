package com.starvocab.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类 - 签发与解析令牌
 */
@Component
public class JwtUtil {

    @Value("${starvocab.jwt.secret}")
    private String secret;

    @Value("${starvocab.jwt.access-expire}")
    private long accessExpire;

    @Value("${starvocab.jwt.refresh-expire}")
    private long refreshExpire;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, String nickname) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("nickname", nickname);
        claims.put("type", "access");
        return build(claims, accessExpire);
    }

    public String generateRefreshToken(Long userId, String nickname) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("nickname", nickname);
        claims.put("type", "refresh");
        return build(claims, refreshExpire);
    }

    private String build(Map<String, Object> claims, long expireSeconds) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireSeconds * 1000);
        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key())
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token) {
        Object id = parse(token).get("userId");
        return id instanceof Number ? ((Number) id).longValue() : Long.valueOf(id.toString());
    }

    public String getNickname(String token) {
        return (String) parse(token).get("nickname");
    }

    public boolean isExpired(String token) {
        try {
            return parse(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
