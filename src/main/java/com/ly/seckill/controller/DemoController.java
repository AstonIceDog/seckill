package com.ly.seckill.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/*
 * @description: some description
 */
@Controller
@RequestMapping("/demo")
public class DemoController {
    /**
     * @description:测试页面跳转
     * @param: [model]
     * @return: java.lang.String
     **/
    @RequestMapping("/hello")
    public String hello(Model model){
        model.addAttribute("name", "ly");
        return "hello";
    }
}
