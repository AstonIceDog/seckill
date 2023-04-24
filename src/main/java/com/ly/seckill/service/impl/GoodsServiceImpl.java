package com.ly.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ly.seckill.mapper.GoodsMapper;
import com.ly.seckill.pojo.Goods;
import com.ly.seckill.service.IGoodsService;
import com.ly.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * @description: some description
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {
    @Autowired
    private GoodsMapper goodsMapper;
    /**
     * @description: 获取商品列表
     * @param: []
     * @return: java.util.List<com.ly.seckill.vo.GoodsVo>
     **/
    @Override
    public List<GoodsVo> findGoodsVo() {
        return goodsMapper.findGoodsVo();
    }


    /**
     * @description: 获取商品详情
     * @param: [GoodsId]
     * @return: java.lang.String
     **/
    @Override
    public GoodsVo findGoodsVoByGoodsId(Long goodsId) {
        return goodsMapper.findGoodsVoByGoodsId(goodsId);
    }
}
