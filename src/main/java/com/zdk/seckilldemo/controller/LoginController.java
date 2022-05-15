package com.zdk.seckilldemo.controller;

import com.zdk.seckilldemo.service.UserService;
import com.zdk.seckilldemo.vo.ApiResp;
import com.zdk.seckilldemo.vo.LoginVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;

/**
 * @author zdk
 * @date 2022/5/15 17:09
 */

@Api(value = "登录",tags = "登录")
@Slf4j
@Controller
@RequestMapping("/login")
public class LoginController extends BaseController{

    @Autowired
    private UserService userService;

    @ApiOperation(value = "跳转登录页面")
    @GetMapping("/toLogin")
    public String toLogin() {
        return "login";
    }

    @ApiOperation(value = "登录接口")
    @PostMapping("/doLogin")
    @ResponseBody
    public ApiResp toLogin(@Valid LoginVo loginVo) {
        log.info("loginVo:{}",loginVo);
        return userService.doLogin(loginVo);
    }

}
