package com.ly.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ly.seckill.pojo.SeckillOrder;
import com.ly.seckill.pojo.User;

/*
 * @description: some description
 */
public interface ISeckillOrderService extends IService<SeckillOrder> {
    /***
     * @description: 获取秒杀结果
     * @param: [user, goodsId]
     * @return: orderId:成功,-1:秒杀失败,0:排队中
     **/
    Long getResult(User user, Long goodsId);
}
