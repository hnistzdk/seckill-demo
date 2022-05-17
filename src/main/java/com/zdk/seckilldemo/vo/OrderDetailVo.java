package com.zdk.seckilldemo.vo;

import com.zdk.seckilldemo.pojo.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author zdk
 * @date 2022/5/17 21:11
 * 订单详情Vo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class OrderDetailVo {
    private GoodsVo goods;
    private Order order;
}
