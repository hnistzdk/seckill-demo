package com.zdk.seckilldemo.controller;


import com.sun.org.apache.xpath.internal.operations.Or;
import com.zdk.seckilldemo.pojo.User;
import com.zdk.seckilldemo.service.GoodsService;
import com.zdk.seckilldemo.service.UserService;
import com.zdk.seckilldemo.utils.RedisUtil;
import com.zdk.seckilldemo.vo.ApiResp;
import com.zdk.seckilldemo.vo.DetailVo;
import com.zdk.seckilldemo.vo.GoodsVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    private GoodsService goodsService;
    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    /**
     * 1000个线程 重复10次，测三次 相当于30000
     *
     * 8核8线程i7-9700的windows
     * 优化前QPS：1835.9/sec
     * 进行页面缓存优化后QPS：4448.4/sec
     *
     * @param model
     * @param user
     * @return
     */
    @ApiOperation(value = "商品页面")
    @GetMapping(value = "/toList",produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toList(Model model, User user){
        //因为使用的了HandlerMethodArgumentResolver
        //就能省略 方法参数获取cookie,再通过cookie找User,再转换的过程
        //而直接将User作为入参进行判断即可
        if (user == null){
            return "login";
        }
        String html = redisUtil.get("goodsList");
        //获取页面 如果不为空 直接返回
        if (isOk(html)){
            return html;
        }
        //获取页面 如果为空 手动渲染 存入redis 再返回
        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());
        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", context);
        if (isOk(html)){
            redisUtil.set("goodsList", html,60);
        }
        return html;
    }

    @ApiOperation(value = "商品详情页")
    @GetMapping(value = "/toDetail2",produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toDetail(Model model,User user,Long goodsId){
        if (user == null){
            return "login";
        }
        //如果存在 直接返回
        String html = redisUtil.get("goodsDetails:"+goodsId);
        if (isOk(html)){
            return html;
        }
        //为空 渲染 缓存 返回
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

        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", context);
        if (isOk(html)){
            redisUtil.set("goodsDetails:"+goodsId, html,60);
        }
        return html;
    }

    @ApiOperation(value = "商品详情页数据接口")
    @GetMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public ApiResp detail(User user,@PathVariable Long goodsId){
        if (user == null){
            return null;
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
        DetailVo detailVo = new DetailVo()
                .setUser(user)
                .setGoodsVo(goodsVo)
                .setSecKillStatus(secKillStatus)
                .setRemainSeconds(remainSeconds)
                .setSeckillSeconds(seckillSeconds);
        return ApiResp.success(detailVo);
    }
}

