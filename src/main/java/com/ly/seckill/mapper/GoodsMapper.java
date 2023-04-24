package com.ly.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ly.seckill.pojo.Goods;
import com.ly.seckill.vo.GoodsVo;

import java.util.List;

/*
 * @description: some description
 */
public interface GoodsMapper extends BaseMapper<Goods> {
    /**
     * @description:
     * @param: []
     * @return: java.util.List<com.ly.seckill.vo.GoodsVo>
     **/
    List<GoodsVo> findGoodsVo();

    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
