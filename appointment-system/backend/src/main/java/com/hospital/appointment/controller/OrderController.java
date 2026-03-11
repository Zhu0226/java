package com.hospital.appointment.controller;

import com.hospital.appointment.annotation.Idempotent;
import com.hospital.appointment.annotation.RateLimit;
import com.hospital.appointment.common.Result;
import com.hospital.appointment.common.UserContext;
import com.hospital.appointment.model.dto.OrderCreateDTO;
import com.hospital.appointment.model.dto.PaymentCallbackDTO;
import com.hospital.appointment.model.vo.PatientOrderVO;
import com.hospital.appointment.service.impl.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderServiceImpl orderService;

    @PostMapping("/create")
    @RateLimit(time = 60, count = 1)
    @Idempotent
    public Result<String> createOrder(@Validated @RequestBody OrderCreateDTO dto) {
        String orderNo = orderService.createAppointmentOrder(dto);
        return Result.success(orderNo);
    }

    @GetMapping("/my-orders")
    public Result<List<PatientOrderVO>> getMyOrders() {
        Long patientId = UserContext.getUserId();
        return Result.success(orderService.getPatientOrders(patientId));
    }

    @PostMapping("/cancel/{orderNo}")
    public Result<Void> cancelOrder(@PathVariable String orderNo) {
        orderService.cancelOrder(orderNo);
        return Result.success();
    }

    /**
     * 【本次新增】：模拟微信支付成功的回调触发器
     */
    @PostMapping("/mock-pay/{orderNo}")
    public Result<Void> mockPay(@PathVariable String orderNo) {
        // 构造一个模拟的第三方支付回调DTO
        PaymentCallbackDTO dto = new PaymentCallbackDTO();
        dto.setOrderNo(orderNo);
        // 生成一个假的微信支付流水号
        dto.setTradeNo("WX_MOCK_" + System.currentTimeMillis());
        dto.setPayMethod(1); // 1-微信

        // 触发核心的支付成功状态机流转
        orderService.processPaymentSuccess(dto);
        return Result.success();
    }
}