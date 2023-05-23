package com.ly.seckill.controller;

import com.ly.seckill.pojo.User;
import com.ly.seckill.rabbitmq.MQSender;
import com.ly.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private MQSender mqSender;

    /***
     * @description: 用户信息(测试)
     * @param: [user]
     * @return: com.ly.seckill.vo.RespBean
     **/
    @RequestMapping("/info")
    @ResponseBody
    public RespBean info(User user){
        return RespBean.success(user);
    }

//    /***
//     * @description: 测试发送RabbitMQ消息
//     * @param: []
//     * @return: void
//     **/
//    @RequestMapping("/mq")
//    @ResponseBody
//    public void mq(){
//        mqSender.send("Hello");
//    }
//
//    /***
//     * @description: Fanout模式
//     * @param: []
//     * @return: void
//     **/
//    @RequestMapping("/mq/fanout")
//    @ResponseBody
//    public void mq01(){
//        mqSender.send("Hello");
//    }
//
//
//    /***
//     * @description: Direct模式
//     * @param: []
//     * @return: void
//     **/
//    @RequestMapping("/mq/direct01")
//    @ResponseBody
//    public void mq02(){
//        mqSender.send01("Hello, Red");
//    }
//
//    /***
//     * @description: Direct模式
//     * @param: []
//     * @return: void
//     **/
//    @RequestMapping("/mq/direct02")
//    @ResponseBody
//    public void mq03(){
//        mqSender.send02("Hello, Green");
//    }
//
//
//    /***
//     * @description: Topic模式
//     * @param: []
//     * @return: void
//     **/
//    @RequestMapping("/mq/topic01")
//    @ResponseBody
//    public void mq04(){
//        mqSender.send03("Hello, Red");
//    }
//
//    /***
//     * @description: Topic模式
//     * @param: []
//     * @return: void
//     **/
//    @RequestMapping("/mq/topic02")
//    @ResponseBody
//    public void mq05(){
//        mqSender.send04("Hello, Green");
//    }
//
//    /***
//     * @description: Headers模式
//     * @param: []
//     * @return: void
//     **/
//    @RequestMapping("/mq/headers01")
//    @ResponseBody
//    public void mq06(){
//        mqSender.send05("Hello, Headers01");
//    }
//
//    /***
//     * @description: Headers模式
//     * @param: []
//     * @return: void
//     **/
//    @RequestMapping("/mq/headers02")
//    @ResponseBody
//    public void mq07(){
//        mqSender.send06("Hello, Headers02");
//    }
}
