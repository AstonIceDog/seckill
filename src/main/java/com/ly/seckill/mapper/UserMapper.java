package com.ly.seckill.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ly.seckill.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}

