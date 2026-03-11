package com.hospital.appointment.mapper;

import com.hospital.appointment.model.entity.SysUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND is_deleted = 0")
    SysUser findByUsername(String username);

    @Insert("INSERT INTO sys_user(id, username, password, role, status, is_deleted, create_time, update_time) " +
            "VALUES(#{id}, #{username}, #{password}, #{role}, 1, 0, NOW(), NOW())")
    int insert(SysUser user);
}