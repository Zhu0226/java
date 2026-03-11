package com.hospital.appointment.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DoctorRegisterDTO {

    @NotBlank(message = "登录账号不能为空")
    @Size(min = 4, max = 20, message = "账号长度必须在4-20个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在6-32个字符之间")
    private String password;

    @NotBlank(message = "医生姓名不能为空")
    private String realName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    private String phone;

    @NotNull(message = "性别不能为空")
    @Min(value = 1, message = "性别值不合法")
    @Max(value = 2, message = "性别值不合法")
    private Integer gender;

    @NotNull(message = "所属科室ID不能为空")
    private Long deptId;

    @NotBlank(message = "职称不能为空")
    private String title;

    @NotNull(message = "从业年限不能为空")
    @Min(value = 0, message = "从业年限不能为负数")
    private Integer experienceYears;

    @NotBlank(message = "擅长领域不能为空")
    @Size(max = 255, message = "擅长领域描述过长")
    private String expertise;

    @NotBlank(message = "医生简介不能为空")
    private String introduction;
}