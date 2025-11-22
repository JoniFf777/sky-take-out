package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {

    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
        log.info("开始创建RedisTemplate对象");
        RedisTemplate redisTemplate = new RedisTemplate();
        // 配置连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // 使用默认的序列化方式
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        return redisTemplate;
    }

}
