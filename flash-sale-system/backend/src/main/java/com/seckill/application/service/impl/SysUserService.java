package com.seckill.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.common.exception.BusinessException;
import com.seckill.common.util.JwtUtils;
import com.seckill.domain.entity.SysUser;
import com.seckill.persistence.mapper.SysMenuMapper;
import com.seckill.persistence.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;// 用户
    @Autowired
    private SysMenuMapper sysMenuMapper;// 菜单
    @Autowired
    private JwtUtils jwtUtils;// JWT
    @Autowired
    private StringRedisTemplate stringRedisTemplate;// Redis

    public Map<String, Object> login(String username, String password) {// 登录
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();// 创建条件
        query.eq(SysUser::getUsername, username).eq(SysUser::getPassword, password);// 查询条件
        SysUser user = sysUserMapper.selectOne(query);// 查询用户

        if (user == null) throw new BusinessException(401, "账号或密码错误");
        if (user.getStatus() == 0) throw new BusinessException(403, "该账号已被冻结");

        List<String> perms = sysMenuMapper.selectPermsByUserId(user.getId());// 查询权限

        stringRedisTemplate.opsForValue().set(
                "auth:perms:" + user.getId(),
                String.join(",", perms),
                2, TimeUnit.HOURS
        );

        String token = jwtUtils.generateToken(String.valueOf(user.getId()));

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("perms", perms);
        return data;
    }
}
