package com.zdk.seckilldemo.mapper;

import com.zdk.seckilldemo.pojo.Goods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zdk.seckilldemo.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 * 商品表 Mapper 接口
 * </p>
 *
 * @author zdk
 * @since 2022-05-15
 */
public interface GoodsMapper extends BaseMapper<Goods> {
    List<GoodsVo> findGoodsVo();
    GoodsVo findGoodsVoById(Long id);
}
