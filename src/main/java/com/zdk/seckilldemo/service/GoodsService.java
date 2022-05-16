package com.zdk.seckilldemo.service;

import com.zdk.seckilldemo.pojo.Goods;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zdk.seckilldemo.vo.GoodsVo;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * <p>
 * 商品表 服务类
 * </p>
 *
 * @author zdk
 * @since 2022-05-15
 */
public interface GoodsService extends IService<Goods> {
    List<GoodsVo> findGoodsVo();
    GoodsVo findGoodsVoById(Long id);
}
