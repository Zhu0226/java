package com.hospital.appointment.model.entity;

import lombok.Data;

@Data
public class SysUser {
    private Long id;
    private String username;
    private String password;
    private String role;
    private Integer status;
}