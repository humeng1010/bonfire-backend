package com.usercenter.constant;

public interface RedisConstant {

    String PROJECT_PREFIX_KEY = "user-center:";
    String USER_RECOMMEND_KEY = "user-center:user:recommend:";
    // 单位分钟
    Long USER_RECOMMEND_TTL = 60L;

    String ADD_TEAM_KEY = "user-center:team:lock:";

    String JOIN_TEAM_LOCK = "user-center:join:lock";

}
