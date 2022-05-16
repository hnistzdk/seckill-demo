package com.zdk.seckilldemo.service.serviceImpl;

import com.zdk.seckilldemo.pojo.Goods;
import com.zdk.seckilldemo.mapper.GoodsMapper;
import com.zdk.seckilldemo.service.GoodsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zdk.seckilldemo.vo.GoodsVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author zdk
 * @since 2022-05-15
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {
    @Resource
    private GoodsMapper goodsMapper;

    @Override
    public List<GoodsVo> findGoodsVo() {
        return goodsMapper.findGoodsVo();
    }

    @Override
    public GoodsVo findGoodsVoById(Long id) {
        return goodsMapper.findGoodsVoById(id);
    }
}
