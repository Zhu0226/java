package com.hospital.appointment.service.impl;

import com.hospital.appointment.common.BusinessException;
import com.hospital.appointment.mapper.DoctorWorkbenchMapper;
import com.hospital.appointment.mapper.OrderMapper;
import com.hospital.appointment.model.dto.ConsultationCompleteDTO;
import com.hospital.appointment.model.entity.Order;
import com.hospital.appointment.model.entity.Schedule;
import com.hospital.appointment.model.vo.DoctorOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorWorkbenchServiceImpl {

    private final DoctorWorkbenchMapper workbenchMapper;
    private final OrderMapper orderMapper;

    public List<DoctorOrderVO> getTodayOrderList(Long doctorId) {
        LocalDate today = LocalDate.now();
        log.info("医生[{}]正在查询 [{}] 的接诊列表", doctorId, today);
        return workbenchMapper.selectDoctorOrdersByDate(doctorId, today);
    }

    @Transactional(rollbackFor = Exception.class)
    public void completeConsultation(ConsultationCompleteDTO dto, Long currentDoctorId) {
        Order order = orderMapper.selectByOrderNo(dto.getOrderNo());
        if (order == null) throw new BusinessException("接诊异常：未找到该订单");

        if (!order.getDoctorId().equals(currentDoctorId)) {
            log.error("高危越权操作：医生[{}]尝试操作不属于自己的订单[{}]", currentDoctorId, dto.getOrderNo());
            throw new BusinessException("您无权操作其他医生的患者订单");
        }
        if (order.getOrderStatus() != 1) {
            throw new BusinessException("该患者当前状态不允许进行接诊操作");
        }

        int updatedRows = workbenchMapper.completeConsultation(order.getOrderNo(), currentDoctorId, order.getVersion());
        if (updatedRows == 0) throw new BusinessException("状态已发生变更，请刷新接诊列表重试");

        log.info("🏥 接诊完成！医生[{}] 成功处理了订单[{}]", currentDoctorId, dto.getOrderNo());
    }

    public List<Schedule> getDoctorSchedule(Long doctorId) {
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        log.info("医生[{}]正在查询 {} 至 {} 的排班列表", doctorId, today, nextWeek);
        return workbenchMapper.selectDoctorSchedule(doctorId, today, nextWeek);
    }

    /**
     * 获取医生历史接诊记录
     */
    public List<DoctorOrderVO> getHistoryOrderList(Long doctorId) {
        log.info("医生[{}]正在查询历史接诊记录", doctorId);
        return workbenchMapper.selectHistoryOrders(doctorId);
    }
}