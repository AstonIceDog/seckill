package com.ly.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ly.seckill.pojo.Order;
import com.ly.seckill.pojo.SeckillOrder;
import com.ly.seckill.pojo.User;
import com.ly.seckill.rabbitmq.MQSender;
import com.ly.seckill.service.IGoodsService;
import com.ly.seckill.service.IOrderService;
import com.ly.seckill.service.ISeckillOrderService;
import com.ly.seckill.utils.JsonUtil;
import com.ly.seckill.vo.GoodsVo;
import com.ly.seckill.vo.RespBean;
import com.ly.seckill.vo.RespBeanEnum;
import com.ly.seckill.vo.SeckillMessage;
import com.rabbitmq.tools.json.JSONUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * @description: some description
 */
@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean {
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MQSender mqSender;
    private Map<Long, Boolean> EmptyStockMap = new HashMap<>();

//    /**
//     * @description: 秒杀
//     * windows优化前qps:1036
//     * linux优化前qps:254
//     * @param: [mode, user, goodsId]
//     * @return: java.lang.String
//     **/
//    @RequestMapping("/doSeckill")
//    public String doSeckill2(Model model, User user, Long goodsId){
//        if (user == null){
//            return "login";
//        }
//        model.addAttribute("user", user);
//        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
//        //判断库存
//        if (goods.getStockCount() < 1){
//            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
//            return "seckillFail";
//        }
//        //判断是否重复抢购
//        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>()
//                .eq("user_id", user.getId())
//                .eq("goods_id", goodsId));
//        if (seckillOrder!=null){
//            model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage());
//            return "seckillFail";
//        }
//        Order order = orderService.seckill(user, goods);
//        model.addAttribute("order", order);
//        model.addAttribute("goods", goods);
//        return "orderDetail";
//    }

    /**
     * @description: 秒杀
     * windows优化前qps:1036
     * linux优化前qps:254
     * windows优化后qps:2510
     * @param: [mode, user, goodsId]
     * @return: java.lang.String
     **/
    @RequestMapping(value = "/doSeckill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSeckill(User user, Long goodsId){
        if (user == null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
//        //数据库查询商品
//        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
//        //判断库存
//        if (goods.getStockCount() < 1){
//            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
//        }
//        //判断是否重复抢购，从redis获取
////        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>()
////                .eq("user_id", user.getId())
////                .eq("goods_id", goodsId));
//
//        SeckillOrder seckillOrder =
//                (SeckillOrder)redisTemplate.opsForValue().get("order:"+user.getId()+":"+goodsId);
//        if (seckillOrder!=null){
//            return RespBean.error(RespBeanEnum.REPEAT_ERROR);
//        }
//        Order order = orderService.seckill(user, goods);
//        return RespBean.success(order);
        //接口优化步骤
        //1.减少数据库访问，判断库存阶段，将商品库存加载到redis，收到秒杀请求，redis预减库存，就不需要进入数据库，如果预减库存不足，则不需要访问数据库
        //2.如果库存足够，请求封装成对象发送给rabbitMQ，异步生成订单，起到流量削峰的作用，返回”排队中“
        //3.封装的异步消息出队，生成订单，减少库存
        //4.客户端轮询，判断是否秒杀成功

        //1.预减库存
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //判断是否重复抢购，从redis获取
        SeckillOrder seckillOrder =
                (SeckillOrder)redisTemplate.opsForValue().get("order:"+user.getId()+":"+goodsId);
        if (seckillOrder!=null){
            return RespBean.error(RespBeanEnum.REPEAT_ERROR);
        }
        //内存标记,减少Redis访问。
        if (EmptyStockMap.get(goodsId)){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //预减库存操作（原子操作）
        Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
        if (stock < 0){
            EmptyStockMap.put(goodsId, true);
            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));
        return RespBean.success(0);
    }


    /***
     * @description: 获取秒杀结果
     * @param: [user, goodsId]
     * @return: orderId:成功,-1:秒杀失败,0:排队中
     **/
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user, Long goodsId){
        if (user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user,goodsId);
        return RespBean.success(orderId);
    }

    /***
     * @description: 初始化,把商品库存数量加载到redis中
     * @param: []
     * @return: void
     **/
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if (CollectionUtils.isEmpty(list)){
            return;
        }
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:"+goodsVo.getId(), goodsVo.getStockCount());
            if (goodsVo.getStockCount() == 0){
                EmptyStockMap.put(goodsVo.getId(), true);
            }else {
                EmptyStockMap.put(goodsVo.getId(), false);
            }
        });
    }
}
