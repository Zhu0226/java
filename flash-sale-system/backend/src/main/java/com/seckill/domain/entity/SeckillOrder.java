package com.seckill.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("seckill_order")
public class SeckillOrder {

    @TableId(type = IdType.AUTO)
    private Long id;// 订单编号
    private Long userId;// 用户编号
    private Long goodsId;// 商品编号
    private Integer status;// 订单状态
    private LocalDateTime createTime;// 创建时间
    private LocalDateTime updateTime;// 更新时间
}
