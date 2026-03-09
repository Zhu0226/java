package com.hospital.appointment.service.impl;

import com.hospital.appointment.common.BusinessException;
import com.hospital.appointment.common.RedisKeyConstants;
import com.hospital.appointment.model.dto.LoginDTO;
import com.hospital.appointment.model.vo.TokenVO;
import com.hospital.appointment.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl {

    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;

    /**
     * 1. 用户登录验证
     */
    public TokenVO login(LoginDTO dto) {
        // 模拟从数据库按用户名查询用户 (真实场景应从 db 获取)
        // 假设数据库里的密文是：$2a$10$wTf7F.j... (对应明文 "123456")
        // SysUser user = userMapper.findByUsername(dto.getUsername());
        Long mockUserId = 888L;
        String mockRole = "PATIENT";
        String mockDbPasswordHash = passwordEncoder.encode("123456");

        // 验证密码 (防彩虹表比对)
        if (!passwordEncoder.matches(dto.getPassword(), mockDbPasswordHash)) {
            throw new BusinessException("账号或密码错误");
        }

        // 密码正确，签发双 Token
        return issueTokens(mockUserId, mockRole);
    }

    /**
     * 2. 无感刷新 Token (核心机制)
     */
    public TokenVO refreshToken(String refreshToken) {
        try {
            // 解析 Refresh Token (自带过期校验)
            Claims claims = JwtUtil.parseToken(refreshToken);

            if (!"REFRESH".equals(claims.get("type", String.class))) {
                throw new BusinessException("非法的 Refresh Token");
            }

            Long userId = claims.get("userId", Long.class);

            // 【核心防线】：去 Redis 校验该 Token 是否在白名单中
            String redisKey = RedisKeyConstants.AUTH_REFRESH_TOKEN + userId;
            Object cachedToken = redisTemplate.opsForValue().get(redisKey);

            if (cachedToken == null || !cachedToken.toString().equals(refreshToken)) {
                // 情况A：Redis 里没找到，说明管理员在后台手动踢了他下线！
                // 情况B：Redis 里的跟传上来的不一样，说明他在别的地方登录被顶号了 (单点登录互踢机制)！
                log.warn("用户 [{}] 的 Refresh Token 已失效或被顶号踢出", userId);
                throw new BusinessException("登录状态已过期，请重新登录");
            }

            // 验证通过，这里为了简便，直接沿用原来的 Role。真实环境最好再去 DB 查一次 Role 防止期间被撤权。
            String role = "PATIENT"; // mock

            log.info("用户 [{}] 无感刷新 Token 成功", userId);
            return issueTokens(userId, role);

        } catch (Exception e) {
            log.error("Refresh Token 校验失败", e);
            throw new BusinessException("请重新登录");
        }
    }

    /**
     * 3. 安全退出登录
     */
    public void logout(Long userId) {
        String redisKey = RedisKeyConstants.AUTH_REFRESH_TOKEN + userId;
        // 斩草除根：删掉 Redis 中的 RT，他就算拿着合法的 RT 也无法再刷新！
        redisTemplate.delete(redisKey);
        log.info("用户 [{}] 安全退出登录", userId);
    }

    /**
     * 辅助方法：统一签发双 Token 并将 RT 放入 Redis 白名单
     */
    private TokenVO issueTokens(Long userId, String role) {
        String accessToken = JwtUtil.generateAccessToken(userId, role);
        String refreshToken = JwtUtil.generateRefreshToken(userId);

        // 将 Refresh Token 存入 Redis，设置 7 天过期。这实现了“单点登录/后登录顶掉前登录”的效果！
        String redisKey = RedisKeyConstants.AUTH_REFRESH_TOKEN + userId;
        redisTemplate.opsForValue().set(redisKey, refreshToken, 7, TimeUnit.DAYS);

        return TokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}