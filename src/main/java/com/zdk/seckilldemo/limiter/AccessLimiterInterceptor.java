package com.zdk.seckilldemo.limiter;

import cn.hutool.json.JSONUtil;
import com.zdk.seckilldemo.pojo.User;
import com.zdk.seckilldemo.service.UserService;
import com.zdk.seckilldemo.utils.CookieUtil;
import com.zdk.seckilldemo.utils.RedisUtil;
import com.zdk.seckilldemo.utils.UserContext;
import com.zdk.seckilldemo.vo.ApiResp;
import com.zdk.seckilldemo.vo.ApiRespEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author zdk
 * @date 2022/5/20 18:26
 */
@Component
public class AccessLimiterInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;
    @Autowired
    private RedisUtil redisUtil;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            User user = getUser(request);
            UserContext.setUser(user);
            AccessLimiter accessLimiter = handlerMethod.getMethodAnnotation(AccessLimiter.class);
            if (accessLimiter == null){
                return true;
            }
            int second = accessLimiter.second();
            int maxCount = accessLimiter.maxCount();
            boolean needLogin = accessLimiter.needLogin();
            String key = "url:"+request.getRequestURI();
            if (needLogin){
                if (user == null){
                    render(response, ApiRespEnum.SESSION_ERROR);
                }else {
                    key += ":"+user.getId();
                }
            }
            Integer count = redisUtil.getNumber(key);
            if (count == null){
                redisUtil.set(key, 1, second);
            }else if (count < maxCount){
                redisUtil.incr(key);
            }else {
                render(response, ApiRespEnum.ACCESS_LIMIT_REACHED);
                return false;
            }
        }
        return true;
    }

    /**
     * 获取用户
     * @param request
     * @return
     */
    public User getUser(HttpServletRequest request){
        String userTicket = CookieUtil.getCookieValue(request, "userTicket");
        if (StringUtils.isBlank(userTicket)){
            return null;
        }
        return userService.getUserByCookie(userTicket);
    }

    /**
     * 响应错误
     * @param response
     * @param apiRespEnum
     * @throws IOException
     */
    public void render(HttpServletResponse response, ApiRespEnum apiRespEnum) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        ApiResp apiResp = ApiResp.error(apiRespEnum);
        writer.write(JSONUtil.toJsonStr(apiResp));
        writer.flush();
        writer.close();
    }
}
