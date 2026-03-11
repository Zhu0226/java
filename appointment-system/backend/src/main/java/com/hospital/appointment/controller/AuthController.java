package com.hospital.appointment.controller;

import com.hospital.appointment.common.Result;
import com.hospital.appointment.common.UserContext;
import com.hospital.appointment.model.dto.DoctorRegisterDTO;
import com.hospital.appointment.model.dto.LoginDTO;
import com.hospital.appointment.model.dto.PatientRegisterDTO;
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

    // 【新增】患者注册接口
    @PostMapping("/register/patient")
    public Result<Void> registerPatient(@Validated @RequestBody PatientRegisterDTO dto) {
        authService.registerPatient(dto);
        return Result.success();
    }

    // 【新增】医生注册接口
    @PostMapping("/register/doctor")
    public Result<Void> registerDoctor(@Validated @RequestBody DoctorRegisterDTO dto) {
        authService.registerDoctor(dto);
        return Result.success();
    }

    @PostMapping("/refresh")
    public Result<TokenVO> refresh(@RequestParam("refreshToken") String refreshToken) {
        return Result.success(authService.refreshToken(refreshToken));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        Long userId = UserContext.getUserId();
        if (userId != null) {
            authService.logout(userId);
        }
        return Result.success();
    }
}