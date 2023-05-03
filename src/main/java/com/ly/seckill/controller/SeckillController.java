package com.ly.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ly.seckill.pojo.Order;
import com.ly.seckill.pojo.SeckillOrder;
import com.ly.seckill.pojo.User;
import com.ly.seckill.service.IGoodsService;
import com.ly.seckill.service.IOrderService;
import com.ly.seckill.service.ISeckillOrderService;
import com.ly.seckill.vo.GoodsVo;
import com.ly.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/*
 * @description: some description
 */
@Controller
@RequestMapping("/seckill")
public class SeckillController {
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IOrderService orderService;

    /**
     * @description: 秒杀
     * windows优化前qps:1036
     * linux优化前qps:254
     * @param: [mode, user, goodsId]
     * @return: java.lang.String
     **/
    @RequestMapping("/doSeckill")
    public String doSeckill(Model model, User user, Long goodsId){
        if (user == null){
            return "login";
        }
        model.addAttribute("user", user);
        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
        //判断库存
        if (goods.getStockCount() < 1){
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return "seckillFail";
        }
        //判断是否重复抢购
        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>()
                .eq("user_id", user.getId())
                .eq("goods_id", goodsId));
        if (seckillOrder!=null){
            model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage());
            return "seckillFail";
        }
        Order order = orderService.seckill(user, goods);
        model.addAttribute("order", order);
        model.addAttribute("goods", goods);
        return "orderDetail";
    }
}
