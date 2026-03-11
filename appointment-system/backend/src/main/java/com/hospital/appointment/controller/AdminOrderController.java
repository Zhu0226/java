package com.hospital.appointment.controller;

import com.hospital.appointment.common.BusinessException;
import com.hospital.appointment.common.Result;
import com.hospital.appointment.common.UserContext;
import com.hospital.appointment.model.vo.AdminOrderVO;
import com.hospital.appointment.service.impl.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping("/latest")
    public Result<List<AdminOrderVO>> getLatestOrders() {
        if (!"ADMIN".equals(UserContext.getRole())) {
            throw new BusinessException("越权：仅超级管理员可访问订单大盘");
        }
        return Result.success(adminOrderService.getLatestOrders());
    }
}