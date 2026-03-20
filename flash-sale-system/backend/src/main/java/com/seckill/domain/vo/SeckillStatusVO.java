package com.seckill.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SeckillStatusVO {
    private Integer status;//秒杀状态
    private String statusDesc;//秒杀状态描述
    private LocalDateTime now;//当前时间
    private LocalDateTime startTime;//秒杀开始时间
    private LocalDateTime endTime;//秒杀结束时间
    private Integer remainStock;//剩余库存
}
