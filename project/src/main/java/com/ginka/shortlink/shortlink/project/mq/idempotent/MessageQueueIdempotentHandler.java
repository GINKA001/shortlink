package com.ginka.shortlink.shortlink.project.mq.idempotent;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 消息队列幂等处理
 */
@Component
@RequiredArgsConstructor
public class MessageQueueIdempotentHandler {
    private final StringRedisTemplate stringRedisTemplate;
    private static final String IDEMPOTENT_KEY_PREFIX = "short-link:idempotent:";

    /**
     * 判断消息是否已处理
     * @param messageId 消息ID
     * @return true:已处理
     */
    public boolean isMessageProcessed(String messageId){
        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key,"0",10, TimeUnit.MINUTES));
    }

    /**
     * 消费成功时设置幂等标识 判断消费是否成功
     * @param messageId 消息标识
     */
    public boolean isAccomplish(String messageId){
        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        return Objects.equals(stringRedisTemplate.opsForValue().get(key),"1");
    }

    public void setAccomplish(String messageId){
        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        stringRedisTemplate.opsForValue().set(key,"1",10, TimeUnit.MINUTES);//10分钟后删除 防挂
    }
    /**
     * 消费异常时删除幂等标识
     * @param messageId 消息标识
     */
    public void delMessageProcessed(String messageId){
        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        stringRedisTemplate.delete( key);
    }

}
