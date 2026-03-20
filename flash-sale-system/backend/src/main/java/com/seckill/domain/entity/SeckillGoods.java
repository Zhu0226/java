package com.seckill.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("seckill_goods")
public class SeckillGoods {

    @TableId(type = IdType.AUTO)// 主键自增
    private Long id;// 商品ID
    private String goodsName;// 商品名称
    private BigDecimal price;// 商品价格
    private Integer stock;// 商品库存
    private LocalDateTime startTime;// 秒杀开始时间
    private LocalDateTime endTime;// 秒杀结束时间
    private Integer onShelf;// 商品上架状态
    private String activityTag;// 活动标签
    private LocalDateTime createTime;// 创建时间
    private LocalDateTime updateTime;// 更新时间
}
