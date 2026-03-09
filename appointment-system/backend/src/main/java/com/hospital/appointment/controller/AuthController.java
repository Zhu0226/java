package com.hospital.appointment.controller;

import com.hospital.appointment.common.Result;
import com.hospital.appointment.common.UserContext;
import com.hospital.appointment.model.dto.LoginDTO;
import com.hospital.appointment.model.vo.TokenVO;
import com.hospital.appointment.service.impl.AuthServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;

    @PostMapping("/login")
    public Result<TokenVO> login(@Validated @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    /**
     * 前端拦截器专用：当收到 401 "token_expired" 时，拿着长效 token 来这换新的
     */
    @PostMapping("/refresh")
    public Result<TokenVO> refresh(@RequestParam("refreshToken") String refreshToken) {
        return Result.success(authService.refreshToken(refreshToken));
    }

    /**
     * 注意：退出登录接口不要加进白名单，必须带着 Access Token 才能调用
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        Long userId = UserContext.getUserId();
        if (userId != null) {
            authService.logout(userId);
        }
        return Result.success();
    }
}