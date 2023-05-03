package com.ly.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.ly.seckill.exception.GlobalException;
import com.ly.seckill.mapper.UserMapper;
import com.ly.seckill.pojo.User;
import com.ly.seckill.service.IUserService;
import com.ly.seckill.utils.CookieUtil;
import com.ly.seckill.utils.MD5Util;
import com.ly.seckill.utils.UUIDUtil;
import com.ly.seckill.utils.ValidatorUtil;
import com.ly.seckill.vo.LoginVo;
import com.ly.seckill.vo.RespBean;
import com.ly.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * <p>
 * 服务实现类
 * </p>
 * 乐字节：专注线上IT培训
 * 答疑老师微信：lezijie
 *
 * @author zhoubin
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 功能描述: 登录
     *
     * @param:
     * @return:
     */
    @Override
    public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
//         //参数校验
//         if (StringUtils.isEmpty(mobile)||StringUtils.isEmpty(password)){
//         	return RespBean.error(RespBeanEnum.LOGIN_ERROR);
//         }
//         if (!ValidatorUtil.isMobile(mobile)){
//         	return RespBean.error(RespBeanEnum.MOBILE_ERROR);
//         }
        //根据手机号获取用户
        User user = userMapper.selectById(mobile);
        if (null == user) {
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        //判断密码是否正确
        if (!MD5Util.formPassToDBPass(password, user.getSalt()).equals(user.getPassword())) {
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        // 生成cookie
        String ticket = UUIDUtil.uuid();
        //将用户信息存入redis
        redisTemplate.opsForValue().set("user:"+ticket, user);
//        request.getSession().setAttribute(ticket, user);
        CookieUtil.setCookie(request, response, "userTicket", ticket);
        return RespBean.success(ticket);
    }

    /**
     * @description: 根据cookie获取用户
     * @param: [userTicket]
     * @return: com.ly.seckill.pojo.User
     **/
    @Override
    public User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isEmpty(userTicket)){
            return null;
        }
        User user = (User) redisTemplate.opsForValue().get("user:" + userTicket);
        if (user != null){
            CookieUtil.setCookie(request, response, "userTicket", userTicket);
        }
        return user;
    }
}
