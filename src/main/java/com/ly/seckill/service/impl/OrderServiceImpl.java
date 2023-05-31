package com.ly.seckill.service.impl;
import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ly.seckill.exception.GlobalException;
import com.ly.seckill.mapper.OrderMapper;
import com.ly.seckill.pojo.Order;
import com.ly.seckill.pojo.SeckillGoods;
import com.ly.seckill.pojo.SeckillOrder;
import com.ly.seckill.pojo.User;
import com.ly.seckill.service.IGoodsService;
import com.ly.seckill.service.IOrderService;
import com.ly.seckill.service.ISeckillGoodsService;
import com.ly.seckill.service.ISeckillOrderService;
import com.ly.seckill.utils.MD5Util;
import com.ly.seckill.utils.UUIDUtil;
import com.ly.seckill.vo.GoodsVo;
import com.ly.seckill.vo.OrderDetailVo;
import com.ly.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/*
 * @description: some description
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    @Autowired
    private ISeckillGoodsService seckillGoodsService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * @description: 秒杀
     * @param: [user, goods]
     * @return: com.ly.seckill.pojo.Order
     **/
    @Transactional
    @Override
    public Order seckill(User user, GoodsVo goods) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //秒杀商品表减库存
        SeckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>().eq("goods_id", goods.getId()));
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
//        seckillGoodsService.updateById(seckillGoods);
        //sql语句做处理，判断库存大于0才会处理
        //秒杀订单数据库加索引
        //判断是否重复抢购，从redis获取
        boolean result = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>().setSql("stock_count = stock_count - 1")
                .eq("goods_id", goods.getId()).gt("stock_count", 0));
        if (seckillGoods.getStockCount() < 1){
            valueOperations.set("isStockEmpty:" + goods.getId(), "0");
            return null;
        }
        //生成订单表
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goods.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goods.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        orderMapper.insert(order);
        //生成秒杀订单表
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(user.getId());
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setGoodsId(goods.getId());
        seckillOrderService.save(seckillOrder);
        redisTemplate.opsForValue().set("order:"+user.getId()+":"+goods.getId(),seckillOrder);
        return order;
    }

    /***
     * @description: 订单详情
     * @param: [orderId]
     * @return: com.ly.seckill.vo.OrderDetailVo
     **/
    @Override
    public OrderDetailVo detail(Long orderId) {
        if (orderId == null){
            throw new GlobalException(RespBeanEnum.ORDER_NOT_EXIST);
        }
        Order order = orderMapper.selectById(orderId);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(order.getGoodsId());
        OrderDetailVo detail = new OrderDetailVo();
        detail.setOrder(order);
        detail.setGoodsVo(goodsVo);
        return detail;
    }

    /***
     * @description: 创建秒杀地址
     * @param: [user, goodsId]
     * @return: java.lang.String
     **/
    @Override
    public String createPath(User user, Long goodsId) {
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        redisTemplate.opsForValue().set("seckillPath:"+user.getId()+":"+goodsId, str, 60, TimeUnit.SECONDS);
        return str;
    }

    /***
     * @description: 校验秒杀地址
     * @param: [user, goodsId, path]
     * @return: java.lang.Boolean
     **/
    @Override
    public Boolean checkPath(User user, Long goodsId, String path) {
        if (user==null||goodsId<0|| StringUtils.isEmpty(path)){
            return false;
        }
        String redisPath = (String) redisTemplate.opsForValue().get("seckillPath:" + user.getId() + ":" + goodsId);
        return path.equals(redisPath);
    }

    @Override
    public boolean checkCaptcha(User user, Long goodsId, String captcha) {
        if (StringUtils.isEmpty(captcha) || user == null || goodsId < 0){
            return false;
        }
        String redisCaptcha = (String) redisTemplate.opsForValue().get("captcha:"+user.getId()+":"+goodsId);
        return captcha.equals(redisCaptcha);
    }
}