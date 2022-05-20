package com.zdk.seckilldemo.limiter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author zdk
 * @date 2022/5/20 18:22
 */
@Target(ElementType.METHOD)
@Retention(RUNTIME)
public @interface AccessLimiter {
    int second() default 5;

    int maxCount() default 5;

    boolean needLogin() default true;
}
