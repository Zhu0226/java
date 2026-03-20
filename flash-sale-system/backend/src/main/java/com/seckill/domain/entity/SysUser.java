package com.seckill.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;// 用户ID
    private String username;// 用户名
    private String password;// 密码
    private String realName;// 真实姓名
    private Integer status;// 状态
    private LocalDateTime createTime;// 创建时间
    private LocalDateTime updateTime;// 更新时间
}
