package com.hospital.appointment.utils;

import org.springframework.stereotype.Component;

/**
 * 经典 Twitter 雪花算法 (Snowflake) - 分布式 ID 生成器
 * 特点：全局唯一、趋势递增、完全在内存中生成，性能极高 (每秒百万级)
 */
@Component
public class SnowflakeIdWorker {

    private final long twepoch = 1288834974657L; // 初始时间截
    private final long workerIdBits = 5L; // 机器id所占的位数
    private final long datacenterIdBits = 5L; // 数据标识id所占的位数
    private final long maxWorkerId = ~(-1L << workerIdBits);
    private final long maxDatacenterId = ~(-1L << datacenterIdBits);
    private final long sequenceBits = 12L; // 序列在id中占的位数
    private final long workerIdShift = sequenceBits;
    private final long datacenterIdShift = sequenceBits + workerIdBits;
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    private final long sequenceMask = ~(-1L << sequenceBits);

    // 实际企业中由机器的 IP 或 MAC 地址分配，这里单机演示默认写死为 1
    private long workerId = 1L;
    private long datacenterId = 1L;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public synchronized long nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("系统时钟回拨，拒绝生成 ID");
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) timestamp = tilNextMillis(lastTimestamp);
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return ((timestamp - twepoch) << timestampLeftShift) | (datacenterId << datacenterIdShift) | (workerId << workerIdShift) | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) timestamp = timeGen();
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }
}