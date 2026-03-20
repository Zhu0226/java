package com.seckill.presentation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckill.application.service.SeckillService;
import com.seckill.common.ApiResponse;
import com.seckill.common.annotation.RequiresPermission;
import com.seckill.domain.entity.SeckillOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 企业级后台管理 - 订单与客诉工作台 API
 */
@RestController
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private SeckillService seckillService;

    /**
     * 订单管理台：多维检索与分页查询
     */
    @RequiresPermission("admin:order:list")
    @GetMapping
    public ApiResponse<Page<SeckillOrder>> listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long goodsId,
            @RequestParam(required = false) Integer status) {

        Page<SeckillOrder> result = seckillService.listOrdersForAdmin(page, size, orderId, userId, goodsId, status);
        return ApiResponse.success(result);
    }

    /**
     * 客服工作台：管理员/客服强制取消订单退款
     */
    @RequiresPermission("admin:order:cancel")
    @PostMapping("/{orderId}/cancel")
    public ApiResponse<String> adminCancelOrder(@PathVariable Long orderId) {
        boolean ok = seckillService.adminCancelOrder(orderId);
        if (ok) {
            return ApiResponse.success("订单强制取消成功，库存已释放！");
        }
        return ApiResponse.error("操作失败：订单不存在或状态已变更");
    }
}