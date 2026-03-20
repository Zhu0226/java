package com.seckill.application.service;

import com.seckill.domain.entity.SeckillGoods;
import com.seckill.domain.entity.SeckillOrder;
import com.seckill.domain.vo.SeckillMetricsVO;
import com.seckill.domain.vo.SeckillStatusVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public interface SeckillService {

    void preHeatStock(Long goodsId, int stock);//预热库存
    List<SeckillGoods> listSeckillGoods();//获取秒杀商品列表
    List<SeckillGoods> listAllGoodsForAdmin();//获取所有商品列表
    SeckillGoods getGoodsDetail(Long goodsId);//获取商品详情
    void saveGoods(SeckillGoods goods);//保存商品
    void updateGoods(SeckillGoods goods);//修改商品
    void removeGoods(Long id);//删除商品
    SeckillStatusVO getSeckillStatus(Long goodsId);//获取秒杀状态
    String createSeckillPath(Long userId, Long goodsId);//创建秒杀路径
    String executeSeckill(Long userId, Long goodsId, String path);//执行秒杀
    void compensateRedisState(Long userId, Long goodsId);//补偿Redis状态
    List<SeckillOrder> listUserOrders(Long userId);//获取用户订单
    SeckillOrder getUserOrderForGoods(Long userId, Long goodsId);//获取用户订单
    boolean payOrder(Long orderId, Long userId);//支付订单
    boolean cancelOrder(Long orderId, Long userId);//取消订单
    SeckillMetricsVO getMetrics();//获取秒杀指标
    /**
     * 【管理端】多维度分页查询订单
     */
    Page<SeckillOrder> listOrdersForAdmin(int page, int size, Long orderId, Long userId, Long goodsId, Integer status);

    /**
     * 【管理端】强制取消订单并释放库存（支持处理客诉）
     */
    boolean adminCancelOrder(Long orderId);
}
