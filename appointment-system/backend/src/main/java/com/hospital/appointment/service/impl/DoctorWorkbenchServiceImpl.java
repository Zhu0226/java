package com.hospital.appointment.service.impl;

import com.hospital.appointment.common.BusinessException;
import com.hospital.appointment.mapper.DoctorWorkbenchMapper;
import com.hospital.appointment.mapper.OrderMapper;
import com.hospital.appointment.model.dto.ConsultationCompleteDTO;
import com.hospital.appointment.model.entity.Order;
import com.hospital.appointment.model.vo.DoctorOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 医生工作台业务逻辑实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorWorkbenchServiceImpl {

    private final DoctorWorkbenchMapper workbenchMapper;
    private final OrderMapper orderMapper;

    /**
     * 查询医生今日的接诊列表
     */
    public List<DoctorOrderVO> getTodayOrderList(Long doctorId) {
        LocalDate today = LocalDate.now();
        log.info("医生[{}]正在查询 [{}] 的接诊列表", doctorId, today);
        return workbenchMapper.selectDoctorOrdersByDate(doctorId, today);
    }

    /**
     * 医生点击“就诊完成”
     * 【安全升级】：强制接收经过上下文校验的 currentDoctorId
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeConsultation(ConsultationCompleteDTO dto, Long currentDoctorId) {
        // 1. 安全防线：根据单号查询原订单数据
        Order order = orderMapper.selectByOrderNo(dto.getOrderNo());
        if (order == null) {
            throw new BusinessException("接诊异常：未找到该订单");
        }

        // 2. 越权拦截：通过上下文传入的 currentDoctorId 进行比对，防止医生 A 去点击完成医生 B 的订单
        if (!order.getDoctorId().equals(currentDoctorId)) {
            log.error("高危越权操作：医生[{}]尝试操作不属于自己的订单[{}]", currentDoctorId, dto.getOrderNo());
            throw new BusinessException("您无权操作其他医生的患者订单");
        }

        // 3. 状态机校验：只有“已预约(1)”的患者才能被完成接诊
        if (order.getOrderStatus() != 1) {
            throw new BusinessException("该患者当前状态不允许进行接诊操作");
        }

        // 4. 乐观锁扭转状态至“已就诊(2)”
        int updatedRows = workbenchMapper.completeConsultation(order.getOrderNo(), currentDoctorId, order.getVersion());
        if (updatedRows == 0) {
            throw new BusinessException("状态已发生变更，请刷新接诊列表重试");
        }

        log.info("🏥 接诊完成！医生[{}] 成功处理了订单[{}]", currentDoctorId, dto.getOrderNo());
    }
}