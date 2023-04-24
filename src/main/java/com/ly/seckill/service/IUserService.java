package com.ly.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ly.seckill.pojo.User;
import com.ly.seckill.vo.LoginVo;
import com.ly.seckill.vo.RespBean;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public interface IUserService extends IService<User> {

    /**
     * @description: 登录
     * @param: [loginVo, request, response]
     * @return: com.ly.seckill.vo.RespBean
     **/
    RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);

    /**
     * @description: 根据cookie获取用户
     * @param: [userTicket]
     * @return: com.ly.seckill.pojo.User
     **/
    User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response);
}

