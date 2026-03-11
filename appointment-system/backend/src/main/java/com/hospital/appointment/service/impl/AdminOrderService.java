package com.hospital.appointment.service.impl;

import com.hospital.appointment.mapper.AdminOrderMapper;
import com.hospital.appointment.model.vo.AdminOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final AdminOrderMapper adminOrderMapper;

    public List<AdminOrderVO> getLatestOrders() {
        log.info("🛡️ 管理员正在请求全院最新订单流水");
        return adminOrderMapper.selectLatestOrders();
    }
}