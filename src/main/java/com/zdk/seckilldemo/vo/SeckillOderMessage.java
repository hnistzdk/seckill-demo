package com.zdk.seckilldemo.vo;

import com.zdk.seckilldemo.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zdk
 * @date 2022/5/18 20:54
 * 异步下单消息类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillOderMessage implements Serializable {
    private User user;
    private Long goodsId;
}
