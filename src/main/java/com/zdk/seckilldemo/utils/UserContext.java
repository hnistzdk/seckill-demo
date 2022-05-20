package com.zdk.seckilldemo.utils;

import com.zdk.seckilldemo.pojo.User;

/**
 * @author zdk
 * @date 2022/5/20 18:48
 */
public class UserContext {
    public static ThreadLocal<User> USER_CONTEXT_HOLDER = new ThreadLocal<>();

    public static void setUser(User user) {
        USER_CONTEXT_HOLDER.set(user);
    }

    public static User getUser() {
        return USER_CONTEXT_HOLDER.get();
    }

    public static void remove() {
        USER_CONTEXT_HOLDER.remove();
    }
}
