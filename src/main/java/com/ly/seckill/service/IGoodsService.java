package com.ly.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ly.seckill.pojo.Goods;
import com.ly.seckill.vo.GoodsVo;

import java.util.List;

/*
 * @description: some description
 */
public interface IGoodsService extends IService<Goods> {
    /**
     * @description: 获取商品列表
     * @param: []
     * @return: java.util.List<com.ly.seckill.vo.GoodsVo>
     **/
    List<GoodsVo> findGoodsVo();

    /**
     * @description: 获取商品详情
     * @param: [GoodsId]
     * @return: java.lang.String
     **/
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
