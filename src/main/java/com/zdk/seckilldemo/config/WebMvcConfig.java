package com.zdk.seckilldemo.config;

import com.zdk.seckilldemo.handler.UserHandlerMethodArgumentResolver;
import com.zdk.seckilldemo.limiter.AccessLimiterInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author zdk
 * @date 2022/5/16 18:09
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private UserHandlerMethodArgumentResolver userHandlerMethodArgumentResolver;
    @Autowired
    private AccessLimiterInterceptor accessLimiterInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessLimiterInterceptor);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        WebMvcConfigurer.super.addViewControllers(registry);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        //User类型的入参判断
        resolvers.add(userHandlerMethodArgumentResolver);
    }
}
