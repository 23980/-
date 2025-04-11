package com.czh.mianshiha.constant;

/**
 * Redis常量
 */
public interface RedisConstant {

    /**
     * 用户签到 redis key 前缀
     */
    String USER_SIGN_IN_REDIS_KEY_PREFIX = "mianshiha:user:signins";

    /**
     * 获取用户签到 redis key
     * @param year
     * @param userId
     * @return
     */
    static String getUserSignInRedisKey(int year,long userId) {
        return String.format("%s:%s:%s", USER_SIGN_IN_REDIS_KEY_PREFIX, year, userId);
    }
}
