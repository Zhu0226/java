package com.hospital.appointment.mapper;

import com.hospital.appointment.model.entity.SysDoctor;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DoctorMapper {
    @Insert("INSERT INTO sys_doctor(id, user_id, real_name, phone, gender, dept_id, title, experience_years, expertise, introduction, is_deleted, create_time, update_time) " +
            "VALUES(#{id}, #{userId}, #{realName}, #{phone}, #{gender}, #{deptId}, #{title}, #{experienceYears}, #{expertise}, #{introduction}, 0, NOW(), NOW())")
    int insert(SysDoctor doctor);

    @Select("SELECT * FROM sys_doctor WHERE user_id = #{userId} AND is_deleted = 0")
    SysDoctor findByUserId(Long userId);

    @Select("SELECT COUNT(1) FROM sys_doctor WHERE phone = #{phone}")
    int checkPhoneExist(String phone);
}