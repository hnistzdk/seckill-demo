package com.zdk.seckilldemo.vo;

import com.zdk.seckilldemo.pojo.Goods;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author zdk
 * @ate 2022/5/16 19:11
 * 商品展示对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class GoodsVo extends Goods {
    private BigDecimal seckillPrice;
    private Integer stockCount;
    private Date startDate;
    private Date endDate;
}
