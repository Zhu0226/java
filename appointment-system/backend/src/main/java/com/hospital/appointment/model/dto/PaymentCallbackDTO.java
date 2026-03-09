package com.hospital.appointment.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentCallbackDTO {
    @NotBlank(message = "业务订单号不能为空")
    private String orderNo;

    @NotBlank(message = "第三方支付流水号不能为空")
    private String tradeNo;

    // 支付方式：1-微信，2-支付宝
    private Integer payMethod;
}