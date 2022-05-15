package com.zdk.seckilldemo.utils;

import org.thymeleaf.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zdk
 * @date 2022/5/15 18:10
 * 数据校验工具类
 */
public class ValidatorUtil {
    private static final Pattern MOBILE_PATTEN = Pattern.compile("[1]([3-9])[0-9]{9}$");

    /**
     * 手机号码校验
     * @param mobile
     * @return
     */
    public static boolean isMobile(String mobile) {
        if (StringUtils.isEmpty(mobile)) {
            return false;
        }
        Matcher matcher = MOBILE_PATTEN.matcher(mobile);
        return matcher.matches();
    }
}
