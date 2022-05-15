package com.zdk.seckilldemo.service.serviceImpl;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
        //将ticket放入cookie返回给前端
        CookieUtil.setCookie(request, response, "userTicket", ticket,60*60);
        return ApiResp.success();
    }
}
