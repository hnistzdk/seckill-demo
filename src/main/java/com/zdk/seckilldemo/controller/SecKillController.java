package com.zdk.seckilldemo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zdk.seckilldemo.pojo.SeckillGoods;
import com.zdk.seckilldemo.pojo.SeckillOrder;
import com.zdk.seckilldemo.pojo.User;
import com.zdk.seckilldemo.service.GoodsService;
import com.zdk.seckilldemo.service.OrderService;
import com.zdk.seckilldemo.service.SeckillOrderService;
import com.zdk.seckilldemo.vo.ApiResp;
import com.zdk.seckilldemo.vo.ApiRespEnum;
import com.zdk.seckilldemo.vo.GoodsVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * @author zdk
 * @date 2022/5/16 21:02
 */
@Api(value = "秒杀",tags = "秒杀")
@Controller
@RequestMapping("/seckill")
public class SecKillController {

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private SeckillOrderService seckillOrderService;
    @Autowired
    private OrderService orderService;

    @ApiOperation(value = "秒杀接口")
    @PostMapping("/doSeckill")
    public String doSeckill(Model model, User user, Long goodsId){
        if (user == null){
            return "login";
        }
        GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);
        //判断库存
        if (goodsVo.getStockCount()<1){
            model.addAttribute("errmsg", ApiRespEnum.EMPTY_STOCK.getMessage());
            return "secKillFail";
        }
        //判断该用户是否已秒杀成功过
        SeckillOrder seckillOrder = seckillOrderService.getOne(
                new QueryWrapper<SeckillOrder>()
                .eq("user_id", user.getId())
                .eq("goods_id", goodsId));
        if (seckillOrder != null){
            model.addAttribute("errmsg", ApiRespEnum.REPEAT_ERROR.getMessage());
            return "secKillFail";
        }
        //进行秒杀
        ApiResp result = orderService.seckill(user, goodsVo);
        if (result.isFail(result)){
            model.addAttribute("errmsg", ApiRespEnum.SESSION_ERROR);
            return "secKillFail";
        }
        model.addAttribute("goods", goodsVo);
        model.addAttribute("order", result.getObject());
        return "orderDetail";
    }
}
