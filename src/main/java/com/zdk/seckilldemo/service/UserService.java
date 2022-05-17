package com.zdk.seckilldemo.service;

import com.zdk.seckilldemo.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zdk.seckilldemo.vo.ApiResp;
import com.zdk.seckilldemo.vo.LoginVo;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author zdk
 * @since 2022-05-15
 */
public interface UserService extends IService<User> {

    /**
     * 登录
     * @param loginVo
     * @return
     */
    ApiResp doLogin(LoginVo loginVo);

    /**
     * 通过userTicket cookie获取用户信息
     * @param ticket
     * @return
     */
    User getUserByCookie(String ticket);

    /**
     * 更新密码
     * 用于测试缓存更新
     * @param userTicket
     * @param password
     * @return
     */
    ApiResp updatePassWord(String userTicket,String password);
}
