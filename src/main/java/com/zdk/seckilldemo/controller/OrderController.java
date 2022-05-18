package com.zdk.seckilldemo.controller;


import com.zdk.seckilldemo.pojo.User;
import com.zdk.seckilldemo.service.OrderService;
import com.zdk.seckilldemo.vo.ApiResp;
import com.zdk.seckilldemo.vo.ApiRespEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author zdk
 * @since 2022-05-15
 */
@Api(value = "订单",tags = "订单")
@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @ApiOperation(value = "订单详情接口")
    @GetMapping("/detail")
    @ResponseBody
    public ApiResp detail(User user,Long orderId){
        if (user == null){
            return ApiResp.error(ApiRespEnum.SESSION_ERROR);
        }
        return ApiResp.success(orderService.detail(orderId));
    }
}

