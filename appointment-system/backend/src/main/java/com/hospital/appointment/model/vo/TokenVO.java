package com.hospital.appointment.model.vo;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenVO {
    private String accessToken;
    private String refreshToken;
}