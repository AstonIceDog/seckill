package com.ly.seckill.rabbitmq;

import com.ly.seckill.pojo.SeckillOrder;
import com.ly.seckill.pojo.User;
import com.ly.seckill.service.IGoodsService;
import com.ly.seckill.service.IOrderService;
import com.ly.seckill.utils.JsonUtil;
import com.ly.seckill.vo.GoodsVo;
import com.ly.seckill.vo.RespBean;
import com.ly.seckill.vo.RespBeanEnum;
import com.ly.seckill.vo.SeckillMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/*
 * @description: some description
 */
@Service
@Slf4j
public class MQReceiver {
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//
//    @RabbitListener(queues = "queue")
//    public void receive(Object msg){
//        log.info("接收消息:" + msg);
//    }
//
//    @RabbitListener(queues = "queue_fanout01")
//    public void receive01(Object msg){
//        log.info("QUEUE01接收消息:" + msg);
//    }
//
//    @RabbitListener(queues = "queue_fanout02")
//    public void receive02(Object msg){
//        log.info("QUEUE02接收消息:" + msg);
//    }
//
//    @RabbitListener(queues = "queue_direct01")
//    public void receive03(Object msg){
//        log.info("QUEUE01接收消息:" + msg);
//    }
//
//    @RabbitListener(queues = "queue_direct02")
//    public void receive04(Object msg){
//        log.info("QUEUE02接收消息:" + msg);
//    }
//
//    @RabbitListener(queues = "queue_topic01")
//    public void receive05(Object msg){
//        log.info("QUEUE01接收消息:" + msg);
//    }
//
//    @RabbitListener(queues = "queue_topic02")
//    public void receive06(Object msg){
//        log.info("QUEUE02接收消息:" + msg);
//    }
//
//    @RabbitListener(queues = "queue_headers01")
//    public void receive07(Message message){
//        log.info("QUEUE01接收Message对象:" + message);
//        log.info("QUEUE01接收消息:" + new String(message.getBody()));
//    }
//
//    @RabbitListener(queues = "queue_headers02")
//    public void receive08(Message message){
//        log.info("QUEUE02接收Message对象:" + message);
//        log.info("QUEUE02接收消息:" + new String(message.getBody()));
//    }

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IOrderService orderService;

    /***
     * @description: 下单操作
     * @param: []
     * @return: void
     **/
    @RabbitListener(queues = "seckillQueue")
    public void receive(String message){
        log.info("接收消息:" + message);
        SeckillMessage seckillMessage = JsonUtil.jsonStr2Object(message, SeckillMessage.class);
        Long goodsId = seckillMessage.getGoodsId();
        User user = seckillMessage.getUser();
        //判断库存
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        if (goodsVo.getStockCount() < 1){
            return;
        }
        //判断是否重复抢购，从redis获取
        SeckillOrder seckillOrder =
                (SeckillOrder)redisTemplate.opsForValue().get("order:"+user.getId()+":"+goodsId);
        if (seckillOrder!=null){
            return;
        }
        //下单
        orderService.seckill(user, goodsVo);
    }
}
