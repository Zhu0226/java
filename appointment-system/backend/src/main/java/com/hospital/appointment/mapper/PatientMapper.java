package com.hospital.appointment.mapper;

import com.hospital.appointment.model.entity.SysPatient;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PatientMapper {
    @Insert("INSERT INTO sys_patient(id, user_id, real_name, id_card, phone, gender, birth_date, is_deleted, create_time, update_time) " +
            "VALUES(#{id}, #{userId}, #{realName}, #{idCard}, #{phone}, #{gender}, #{birthDate}, 0, NOW(), NOW())")
    int insert(SysPatient patient);

    @Select("SELECT * FROM sys_patient WHERE user_id = #{userId} AND is_deleted = 0")
    SysPatient findByUserId(Long userId);

    // 【本次新增】：根据业务主键查询患者信息
    @Select("SELECT * FROM sys_patient WHERE id = #{id} AND is_deleted = 0")
    SysPatient findById(Long id);

    @Select("SELECT COUNT(1) FROM sys_patient WHERE phone = #{phone} OR id_card = #{idCard}")
    int checkPhoneOrIdCardExist(@Param("phone") String phone, @Param("idCard") String idCard);
}