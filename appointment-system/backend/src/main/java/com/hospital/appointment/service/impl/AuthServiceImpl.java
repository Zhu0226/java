package com.hospital.appointment.service.impl;

import com.hospital.appointment.common.BusinessException;
import com.hospital.appointment.common.RedisKeyConstants;
import com.hospital.appointment.mapper.DoctorMapper;
import com.hospital.appointment.mapper.PatientMapper;
import com.hospital.appointment.mapper.UserMapper;
import com.hospital.appointment.model.dto.DoctorRegisterDTO;
import com.hospital.appointment.model.dto.LoginDTO;
import com.hospital.appointment.model.dto.PatientRegisterDTO;
import com.hospital.appointment.model.entity.SysDoctor;
import com.hospital.appointment.model.entity.SysPatient;
import com.hospital.appointment.model.entity.SysUser;
import com.hospital.appointment.model.vo.TokenVO;
import com.hospital.appointment.utils.JwtUtil;
import com.hospital.appointment.utils.SnowflakeIdWorker;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl {

    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final PatientMapper patientMapper;
    private final DoctorMapper doctorMapper;
    private final SnowflakeIdWorker snowflakeIdWorker;

    /**
     * 【企业级真实登录】
     */
    public TokenVO login(LoginDTO dto) {
        // 1. 从数据库基础表查询账号
        SysUser user = userMapper.findByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException("账号或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 2. 校验 BCrypt 密文密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }

        // 3. 【核心映射】Token 中签发的 userId 必须是业务主键(patient_id/doctor_id)，而不是鉴权表的 sys_user.id
        Long businessId = user.getId(); // ADMIN 默认用 userId

        if ("PATIENT".equals(user.getRole())) {
            SysPatient patient = patientMapper.findByUserId(user.getId());
            if (patient == null) throw new BusinessException("患者业务数据异常");
            businessId = patient.getId();
        } else if ("DOCTOR".equals(user.getRole())) {
            SysDoctor doctor = doctorMapper.findByUserId(user.getId());
            if (doctor == null) throw new BusinessException("医生业务数据异常");
            businessId = doctor.getId();
        }

        // 4. 签发双 Token
        return issueTokens(businessId, user.getRole());
    }

    /**
     * 【新增：患者注册】使用 @Transactional 保证多表写入一致性
     */
    @Transactional(rollbackFor = Exception.class)
    public void registerPatient(PatientRegisterDTO dto) {
        // 1. 唯一性校验
        if (userMapper.findByUsername(dto.getUsername()) != null) {
            throw new BusinessException("该账号已被注册");
        }
        if (patientMapper.checkPhoneOrIdCardExist(dto.getPhone(), dto.getIdCard()) > 0) {
            throw new BusinessException("该手机号或身份证已注册过账号");
        }

        // 2. 写入 sys_user (基础鉴权表)
        SysUser user = new SysUser();
        user.setId(snowflakeIdWorker.nextId());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // BCrypt加密
        user.setRole("PATIENT");
        userMapper.insert(user);

        // 3. 写入 sys_patient (业务表)
        SysPatient patient = new SysPatient();
        patient.setId(snowflakeIdWorker.nextId());
        patient.setUserId(user.getId()); // 绑定外键
        patient.setRealName(dto.getRealName());
        patient.setIdCard(dto.getIdCard());
        patient.setPhone(dto.getPhone());
        patient.setGender(dto.getGender());
        patient.setBirthDate(dto.getBirthDate());
        patientMapper.insert(patient);
    }

    /**
     * 【新增：医生注册】
     */
    @Transactional(rollbackFor = Exception.class)
    public void registerDoctor(DoctorRegisterDTO dto) {
        if (userMapper.findByUsername(dto.getUsername()) != null) {
            throw new BusinessException("该账号已被注册");
        }
        if (doctorMapper.checkPhoneExist(dto.getPhone()) > 0) {
            throw new BusinessException("该手机号已注册过账号");
        }

        SysUser user = new SysUser();
        user.setId(snowflakeIdWorker.nextId());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole("DOCTOR");
        userMapper.insert(user);

        SysDoctor doctor = new SysDoctor();
        doctor.setId(snowflakeIdWorker.nextId());
        doctor.setUserId(user.getId());
        doctor.setRealName(dto.getRealName());
        doctor.setPhone(dto.getPhone());
        doctor.setGender(dto.getGender());
        doctor.setDeptId(dto.getDeptId());
        doctor.setTitle(dto.getTitle());
        doctor.setExperienceYears(dto.getExperienceYears());
        doctor.setExpertise(dto.getExpertise());
        doctor.setIntroduction(dto.getIntroduction());
        doctorMapper.insert(doctor);
    }

    // --- 下面原有的 refreshToken, logout, issueTokens 方法保持不变 ---
    public TokenVO refreshToken(String refreshToken) {
        // ... 原代码不变 ...
        try {
            Claims claims = JwtUtil.parseToken(refreshToken);
            if (!"REFRESH".equals(claims.get("type", String.class))) throw new BusinessException("非法的 Refresh Token");
            Long userId = claims.get("userId", Long.class);
            String redisKey = RedisKeyConstants.AUTH_REFRESH_TOKEN + userId;
            Object cachedToken = redisTemplate.opsForValue().get(redisKey);
            if (cachedToken == null || !cachedToken.toString().equals(refreshToken)) {
                throw new BusinessException("登录状态已过期，请重新登录");
            }
            // 注意：真实场景刷新token最好查一次库获取最新role，这里为了简便我们直接从token中解析或写个默认
            String role = "PATIENT"; // 这里可以优化为查库获取
            return issueTokens(userId, role);
        } catch (Exception e) {
            throw new BusinessException("请重新登录");
        }
    }

    public void logout(Long userId) {
        redisTemplate.delete(RedisKeyConstants.AUTH_REFRESH_TOKEN + userId);
    }

    private TokenVO issueTokens(Long userId, String role) {
        String accessToken = JwtUtil.generateAccessToken(userId, role);
        String refreshToken = JwtUtil.generateRefreshToken(userId);
        redisTemplate.opsForValue().set(RedisKeyConstants.AUTH_REFRESH_TOKEN + userId, refreshToken, 7, TimeUnit.DAYS);
        return TokenVO.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }
}