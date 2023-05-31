package com.ly.seckill.controller;

import com.baomidou.kaptcha.Kaptcha;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ly.seckill.config.AccessLimit;
import com.ly.seckill.exception.GlobalException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/*
 * @description: some description
 */
@Slf4j
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
    @Autowired
    private RedisScript<Long> script;
    @Autowired
    private Kaptcha captcha;

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
     * 优化前qps:1036
     * 缓存qps:2510
     * 优化qps:3730
     * @param: [mode, user, goodsId]
     * @return: java.lang.String
     **/
    @RequestMapping(value = "/{path}/doSeckill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSeckill(@PathVariable String path, User user, Long goodsId){
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
        Boolean check = orderService.checkPath(user, goodsId, path);
        if (!check){
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }
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
//        Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
        Long stock = (Long) redisTemplate.execute(script, Collections.singletonList("seckillGoods:" + goodsId), Collections.EMPTY_LIST);
        if (stock < 0){
            EmptyStockMap.put(goodsId, true);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));
        return RespBean.success(0);
//      优化目的：减少数据库访问
//1. 初始化阶段：将商品数量加载到redis中
//2. 商品秒杀阶段，收到秒杀请求，到redis中预减库存，如果库存不足，直接拒绝，否则进入下一步
//3. 将请求加入队列，返回排对中
//4. 队列中的消息被消费者监听消费，生成订单，减少库存，客户端轮询是否下单成功
//分布式锁使用lua脚本确保原子性
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
     * @description: 获取秒杀地址
     * @param: [user, goodsId]
     * @return: com.ly.seckill.vo.RespBean
     **/
    @AccessLimit(second=5, maxCount=5, needLogin=true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(User user, Long goodsId, String captcha, HttpServletRequest request){
        if (user == null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        boolean check = orderService.checkCaptcha(user, goodsId, captcha);
        if (!check){
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }
        String str = orderService.createPath(user, goodsId);
        return RespBean.success(str);
    }

    @RequestMapping(value = "/captcha", method = RequestMethod.GET)
    public void verifyCode(User user, Long goodsId, HttpServletResponse response){
        if (user == null || goodsId < 0){
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        //设置请求头为输出图片的类型
        response.setContentType("image/jpg");
        response.setHeader("Pargam", "No-cache");
        response.setHeader("Cache-Control", "No-cache");
        response.setDateHeader("Expires", 0);
        //生成验证码，将结果放入redis
        String captchaText = captcha.render();
        redisTemplate.opsForValue().set("captcha:"+user.getId()+":"+goodsId, captchaText, 300, TimeUnit.SECONDS);
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
