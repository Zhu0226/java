package com.hospital.appointment.model.entity;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SysPatient {
    private Long id;
    private Long userId;
    private String realName;
    private String idCard;
    private String phone;
    private Integer gender;
    private LocalDate birthDate;
}