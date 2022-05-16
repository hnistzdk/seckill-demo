package com.zdk.seckilldemo.controller;


import com.zdk.seckilldemo.pojo.User;
import com.zdk.seckilldemo.service.GoodsService;
import com.zdk.seckilldemo.service.UserService;
import com.zdk.seckilldemo.utils.RedisUtil;
import com.zdk.seckilldemo.vo.ApiResp;
import com.zdk.seckilldemo.vo.GoodsVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * <p>
 * 商品表 前端控制器
 * </p>
 *
 * @author zdk
 * @since 2022-05-15
 */
@Api(value = "商品",tags = "商品")
@Controller
@RequestMapping("/goods")
public class GoodsController extends BaseController{

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserService userService;
    @Autowired
    private GoodsService goodsService;

    @ApiOperation(value = "商品页面")
    @GetMapping("/toList")
    public String toList(Model model,User user){
        //因为使用的了HandlerMethodArgumentResolver
        //就能省略 方法参数获取cookie,再通过cookie找User,再转换的过程
        //而直接将User作为入参进行判断即可
        if (user == null){
            return "login";
        }
        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());
        return "goodsList";
    }

    @ApiOperation(value = "商品详情页")
    @GetMapping("/toDetail")
    public String toDetail(Model model,User user,Long goodsId){
        if (user == null){
            return "login";
        }
        GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date now = new Date();
        int secKillStatus;
        //秒杀倒计时秒数
        long remainSeconds;
        //秒杀持续时间
        long seckillSeconds = 0;
        //秒杀未开始
        if (now.before(startDate)){
            secKillStatus = 0;
            remainSeconds = (startDate.getTime()-now.getTime())/1000;
        } else if (now.after(endDate)){
            //秒杀已结束
            secKillStatus = 2;
            remainSeconds = -1;
        } else{
            //秒杀进行中
            seckillSeconds = (endDate.getTime()-now.getTime())/1000;
            secKillStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("seckillSeconds", seckillSeconds);
        model.addAttribute("secKillStatus", secKillStatus);
        model.addAttribute("remainSeconds", remainSeconds);
        model.addAttribute("user", user);
        model.addAttribute("goods", goodsVo);
        return "goodsDetail";
    }
}

