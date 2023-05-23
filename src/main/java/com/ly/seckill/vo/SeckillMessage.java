package com.ly.seckill.vo;

import com.ly.seckill.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @description: some description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillMessage {

    private User user;

    private Long goodsId;
}
