package com.zdk.seckilldemo.handler;

import com.zdk.seckilldemo.pojo.User;
import com.zdk.seckilldemo.service.UserService;
import com.zdk.seckilldemo.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zdk
 * @date 2022/5/16 18:19
 * 对Controller中的方法中的  User类型的参数做统一判断
 */
@Component
public class UserHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private UserService userService;

    /**
     * 如果参数的类型是User 才交由resolveArgument方法处理
     * @param parameter
     * @return
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> clazz = parameter.getParameterType();
        return clazz == User.class;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String userTicket = CookieUtil.getCookieValue(request, "userTicket");
        if (StringUtils.isBlank(userTicket)){
            return null;
        }
        return userService.getUserByCookie(userTicket);
    }
}
