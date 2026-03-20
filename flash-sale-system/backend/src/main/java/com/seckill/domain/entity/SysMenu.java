package com.seckill.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_menu")
public class SysMenu {

    @TableId(type = IdType.AUTO)
    private Long id;// 菜单ID
    private Long parentId;// 父菜单ID
    private String menuName;// 菜单名称
    private String perms;// 权限标识
    private Integer type;// 菜单类型
    private LocalDateTime createTime;// 创建时间
}
