package com.vticket.vticket.config.redis;

public class RedisKey {
    public static final String REDIS_LIST_CATEGORY = "vt:list:cate";
    public static final String REDIS_CATEGORY_BY_ID = "vt:cate:%s";
    public static final String ACCESS_TOKEN = "vt:payment:token:";
    public static final String USER_ID = "vt:user:id:";
    public static final String USER_TYPE_LOGIN = "vt:users:";
    public static final String OTP_EMAIL = "vt:otp:email:%s";
    public static final String PENDING_USER_EMAIL = "vt:pending:user:%s";
    public static final String REDIS_LIST_EVENT = "vt:list:event:";
    public static final String REDIS_EVENT_BY_ID = "vt:event:%s";
    public static final String SEAT_LOCK = "vt:seat:lock:" ;
    public static final String SEAT_HOLD = "vt:seat:hold:";
}
