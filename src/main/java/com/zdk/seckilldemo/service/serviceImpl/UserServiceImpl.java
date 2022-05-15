package com.zdk.seckilldemo.service.serviceImpl;

import com.zdk.seckilldemo.pojo.User;
import com.zdk.seckilldemo.mapper.UserMapper;
import com.zdk.seckilldemo.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
