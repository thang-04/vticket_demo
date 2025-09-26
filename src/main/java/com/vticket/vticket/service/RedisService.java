package com.vticket.vticket.service;

import com.vticket.vticket.config.RedisKey;
import com.vticket.vticket.domain.mongodb.entity.User;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    private final Logger logger = Logger.getLogger(RedisService.class);

    @Getter
    @Setter
    @Autowired
    private StringRedisTemplate redisSsoUser;

    public void deleteRedisUser(User user) {
        String keyRedis = RedisKey.USER_ID + user.getId();
        this.getRedisSsoUser().delete(keyRedis);
        logger.info("Deleted Redis key: " + keyRedis);
    }
}
