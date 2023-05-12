package com.ly.seckill.controller;

import com.ly.seckill.pojo.User;
import com.ly.seckill.service.IOrderService;
import com.ly.seckill.vo.OrderDetailVo;
import com.ly.seckill.vo.RespBean;
import com.ly.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/*
 * @description: some description
 */
@Controller
@RequestMapping("/order")
public class OrderController {


    @Autowired
    private IOrderService orderService;

    /***
     * @description: 订单详情
     * @param: [user, orderId]
     * @return: com.ly.seckill.vo.RespBean
     **/
    @RequestMapping("/detail")
    @ResponseBody
    public RespBean detail(User user, Long orderId){
        if (user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        OrderDetailVo detail = orderService.detail(orderId);
        return RespBean.success(detail);
    }

}
