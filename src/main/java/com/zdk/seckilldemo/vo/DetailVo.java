package com.zdk.seckilldemo.vo;

import com.zdk.seckilldemo.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author zdk
 * @date 2022/5/17 20:28
 * 商品详情Vo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DetailVo {
    private User user;
    private GoodsVo goodsVo;

    private int secKillStatus;

    private long remainSeconds;
    private long seckillSeconds;
}
