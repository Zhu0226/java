package com.seckill.domain.vo;

import lombok.Data;

@Data
public class SeckillMetricsVO {
    private Long totalRequests;//总请求数
    private Long successCount;//成功数
    private Long failCount;//失败数
    private Double successRate;//成功率
}
