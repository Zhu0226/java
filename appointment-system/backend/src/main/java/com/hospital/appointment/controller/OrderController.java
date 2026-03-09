package com.hospital.appointment.controller;

import com.hospital.appointment.annotation.Idempotent;
import com.hospital.appointment.annotation.RateLimit;
import com.hospital.appointment.common.Result;
import com.hospital.appointment.model.dto.OrderCancelDTO;
import com.hospital.appointment.model.dto.OrderCreateDTO;
import com.hospital.appointment.model.dto.PaymentCallbackDTO;
import com.hospital.appointment.service.impl.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 挂号订单 API 接口
 */
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderServiceImpl orderService;

    /**
     * 1. 患者发起挂号请求
     * 【安全武装】：
     * - @RateLimit: 同一个用户 60 秒内最多点 3 次，防止黄牛写脚本狂刷接口。
     * - @Idempotent: 5 秒内如果发送完全一样的报文，直接拦截，防手抖连点。
     */
    @RateLimit(time = 60, count = 3, message = "抢号太频繁了，请一分钟后再试！")
    @Idempotent(expireTime = 5, message = "您已提交过订单，系统正在处理中，请勿连点")
    @PostMapping("/create")
    public Result<String> createAppointment(@Validated @RequestBody OrderCreateDTO dto) {
        String orderNo = orderService.createAppointmentOrder(dto);
        return Result.success(orderNo);
    }

    /**
     * 2. 模拟第三方支付成功回调接口
     * 【安全武装】：
     * 微信可能因为网络波动对同一个订单发起多次回调，我们通过幂等性拦截瞬间并发的重复报文
     */
    @Idempotent(expireTime = 10, message = "支付结果处理中")
    @PostMapping("/pay/callback")
    public Result<Void> payCallback(@Validated @RequestBody PaymentCallbackDTO dto) {
        orderService.processPaymentSuccess(dto);
        return Result.success();
    }

    /**
     * 3. 患者主动取消订单接口
     * 取消属于敏感操作，也必须防重
     */
    @Idempotent(expireTime = 3)
    @PostMapping("/cancel")
    public Result<Void> cancelOrder(@Validated @RequestBody OrderCancelDTO dto) {
        orderService.cancelByPatient(dto);
        return Result.success();
    }
}