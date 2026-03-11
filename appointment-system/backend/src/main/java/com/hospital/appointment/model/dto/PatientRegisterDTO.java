package com.hospital.appointment.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PatientRegisterDTO {

    @NotBlank(message = "登录账号不能为空")
    @Size(min = 4, max = 20, message = "账号长度必须在4-20个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在6-32个字符之间")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(min = 2, max = 20, message = "姓名长度不合理")
    private String realName;

    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^(^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$)|(^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])((\\d{4})|\\d{3}[Xx])$)$",
            message = "身份证号码格式不正确")
    private String idCard;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    private String phone;

    @NotNull(message = "性别不能为空")
    @Min(value = 1, message = "性别值不合法")
    @Max(value = 2, message = "性别值不合法")
    private Integer gender;

    @NotNull(message = "出生日期不能为空")
    @Past(message = "出生日期必须是过去的时间")
    private LocalDate birthDate;
}