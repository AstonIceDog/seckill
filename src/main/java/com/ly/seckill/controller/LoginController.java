package com.ly.seckill.controller;

import com.ly.seckill.service.IUserService;
import com.ly.seckill.vo.LoginVo;
import com.ly.seckill.vo.RespBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;


/**
 * @description: 登录
 * @param:
 * @return:
 **/
@Controller
@RequestMapping("/login")
@Slf4j
public class LoginController {

    @Autowired
    private IUserService userService;

    /**
     * @description: 跳转登录页面
     * @param: []
     * @return: java.lang.String
     **/
    @RequestMapping("/toLogin")
    public String toLogin(){
        return "login";
    }

    /**
     * @description: 登录功能
     * @param: [loginVo]
     * @return: com.ly.seckill.vo.RespBean
     **/
    @RequestMapping("/doLogin")
    @ResponseBody
    public RespBean doLogin(@Valid LoginVo loginVo, HttpServletRequest request, HttpServletResponse response){
        return userService.doLogin(loginVo, request, response);
    }

}
