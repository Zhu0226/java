package com.hospital.appointment.model.entity;

import lombok.Data;

@Data
public class SysDoctor {
    private Long id;
    private Long userId;
    private String realName;
    private String phone;
    private Integer gender;
    private Long deptId;
    private String title;
    private Integer experienceYears;
    private String expertise;
    private String introduction;
}