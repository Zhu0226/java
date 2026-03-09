package com.hospital.appointment.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.util.Date;

/**
 * 升级版 双 Token 架构 JWT 工具类
 */
@Slf4j
public class JwtUtil {

    private static final String SECRET_STRING = "HospitalEnterpriseLevelSecretKey2024ForSecurityProtection!";
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());

    // Access Token 过期时间：极致安全，仅 15 分钟！
    public static final long ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000;

    // Refresh Token 过期时间：为了用户体验，设为 7 天
    public static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 1. 生成 Access Token (短效，用于请求接口)
     */
    public static String generateAccessToken(Long userId, String role) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .claim("type", "ACCESS")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 2. 生成 Refresh Token (长效，专用于换取新的 Access Token)
     */
    public static String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("type", "REFRESH")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 3. 解析并校验 Token
     */
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}