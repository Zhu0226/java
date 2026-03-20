package com.seckill.application.service.impl;

import com.seckill.common.ErrorCode;
import com.seckill.common.exception.BusinessException;
import com.seckill.domain.entity.SeckillOrder;
import com.seckill.application.service.OrderService;
import com.seckill.persistence.mapper.SeckillGoodsMapper;
import com.seckill.persistence.mapper.SeckillOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private SeckillGoodsMapper goodsMapper;// 商品
    @Autowired
    private SeckillOrderMapper orderMapper;// 订单

    @Override
    @Transactional(rollbackFor = Exception.class)// 开启事务
    public void createOrder(Long userId, Long goodsId) {
        int dbAffected = goodsMapper.reduceStock(goodsId);// 数据库减库存
        if (dbAffected == 0) {
            throw new BusinessException(ErrorCode.STOCK_SOLD_OUT, "数据库库存不足");
        }
        try {
            SeckillOrder order = new SeckillOrder();// 创建订单
            order.setUserId(userId);// 用户ID
            order.setGoodsId(goodsId);// 商品ID
            order.setStatus(0);// 订单状态
            orderMapper.insert(order);// 插入订单
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.ALREADY_ORDERED);// 重复订单
        }
    }
}
