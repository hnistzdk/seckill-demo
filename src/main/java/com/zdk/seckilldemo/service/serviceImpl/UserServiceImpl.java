package com.zdk.seckilldemo.service.serviceImpl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zdk.seckilldemo.exception.GlobalException;
import com.zdk.seckilldemo.mapper.UserMapper;
import com.zdk.seckilldemo.pojo.User;
import com.zdk.seckilldemo.service.UserService;
import com.zdk.seckilldemo.utils.CookieUtil;
import com.zdk.seckilldemo.utils.Md5Util;
import com.zdk.seckilldemo.utils.RedisUtil;
import com.zdk.seckilldemo.vo.ApiResp;
import com.zdk.seckilldemo.vo.ApiRespEnum;
import com.zdk.seckilldemo.vo.LoginVo;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author zdk
 * @since 2022-05-15
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    public HttpServletRequest request;
    @Autowired
    public HttpServletResponse response;

    @Resource
    private UserMapper userMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public ApiResp doLogin(LoginVo loginVo) {
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        User user = userMapper.selectById(mobile);
        //如果用户不存在
        if (user == null){
            throw new GlobalException(ApiRespEnum.MOBILE_NOT_EXIST);
        }
        //存在 加密验证
        if (!Md5Util.encrypt(password, user.getSalt()).equals(user.getPassword())){
            throw new GlobalException(ApiRespEnum.LOGIN_ERROR);
        }
        //生成一个ticket 并设置过期时间
        String ticket = UUID.randomUUID().toString();
        //存入redis
        redisUtil.set("user:"+ticket, user, 60*60);
        //使用了Spring Session后  set到Session的值 会被加入到redis中
//        request.getSession().setAttribute("user:"+ticket, user);
        //将ticket放入cookie返回给前端
        CookieUtil.setCookie(request, response, "userTicket", ticket,60*60);
        return ApiResp.success(ticket);
    }

    @Override
    public User getUserByCookie(String ticket) {
        String userStr = redisUtil.get("user:" + ticket);
        if (StringUtils.isBlank(userStr)){
            return null;
        }
        //存在  刷新用户cookie时间和redis过期时间
        CookieUtil.setCookie(request, response, "userTicket", ticket, 60*60);
        redisUtil.expire("user"+ticket, 60*60);
        return JSONUtil.toBean(userStr, User.class);
    }

    @Override
    public ApiResp updatePassWord(String userTicket, String password) {
        User user = getUserByCookie(userTicket);
        if (user == null){
            throw new GlobalException(ApiRespEnum.MOBILE_NOT_EXIST);
        }
        user.setPassword(Md5Util.encrypt(password, user.getSalt()));
        int update = userMapper.updateById(user);
        if (update>0){
            //同时更新cookie和redis中的user
            CookieUtil.setCookie(request, response, "userTicket", userTicket, 60*60);
            redisUtil.set("user:"+userTicket, user,60*60);
            return ApiResp.success();
        }
        return ApiResp.error(ApiRespEnum.PASSWORD_UPDATE_FAIL);
    }


}
