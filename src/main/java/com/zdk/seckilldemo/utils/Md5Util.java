package com.zdk.seckilldemo.utils;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author zdk
 * @date 2022/5/15 15:43
 * MD5(MD5(pass明文+固定salt)+salt)
 */
public class Md5Util {

    /**
     * 前端加密md5所使用的盐
     */
    public static final String SALT = "1a2b3c4d";

    /**
     * md5方法
     * @param src
     * @return
     */
    public static String md5(String src){
        return DigestUtils.md5Hex(src);
    }


    /**
     * 第一次加密
     * MD5(pass明文+固定salt)
     * @param inputPass 明文密码
     * @return
     */
    public static String firstEncryption(String inputPass){
        String str = SALT.charAt(0)+SALT.charAt(2)+inputPass+SALT.charAt(5)+SALT.charAt(4);
        return md5(str);
    }

    /**
     * 第二次加密
     * MD5(MD5(fromPass+salt)
     * @param inputPass 明文
     * @param salt 随机生成的盐
     * @return
     */
    public static String encrypt(String inputPass,String salt){
        String firstPass = firstEncryption(inputPass);
        return md5(firstPass+salt);
    }
}
