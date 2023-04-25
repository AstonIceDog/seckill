package com.ly.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ly.seckill.pojo.Order;
import com.ly.seckill.pojo.User;
import com.ly.seckill.vo.GoodsVo;

/*
 * @description: some description
 */
public interface IOrderService extends IService<Order> {
    /**
     * @description: 秒杀
     * @param: [user, goods]
     * @return: com.ly.seckill.pojo.Order
     **/
    Order seckill(User user, GoodsVo goods);
}
