package com.ly.seckill.controller;

import com.ly.seckill.pojo.User;
import com.ly.seckill.vo.RespBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/user")
public class UserController {

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
}
