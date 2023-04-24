package com.ly.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ly.seckill.mapper.OrderMapper;
import com.ly.seckill.pojo.Order;
import com.ly.seckill.service.IOrderService;
import org.springframework.stereotype.Service;

/*
 * @description: some description
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {
}