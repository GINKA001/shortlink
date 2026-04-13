package com.ginka.shortlink.shortlink.project.mq.consumer;

import com.ginka.shortlink.shortlink.project.common.convention.exception.ServiceException;
import com.ginka.shortlink.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.ginka.shortlink.shortlink.project.mq.idempotent.MessageQueueIdempotentHandler;
import com.ginka.shortlink.shortlink.project.service.ShortLinkService;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;
import static com.ginka.shortlink.shortlink.project.common.constant.RedisKeyConstant.DELAY_QUEUE_STATS_KEY;
@Slf4j
@Component
@RequiredArgsConstructor
public class DelayShortLinkStatsConsumer implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(DelayShortLinkStatsConsumer.class);
    private final RedissonClient redissonClient;
    private final ShortLinkService shortLinkService;
    private final MessageQueueIdempotentHandler messageQueueIdempotentHandler;
    public void onMessage() {
        Executors.newSingleThreadExecutor(
                        runnable -> {
                            Thread thread = new Thread(runnable);
                            thread.setName("delay_short-link_stats_consumer");
                            thread.setDaemon(Boolean.TRUE);
                            return thread;
                        })
                .execute(() -> {
                    RBlockingDeque<ShortLinkStatsRecordDTO> blockingDeque = redissonClient.getBlockingDeque(DELAY_QUEUE_STATS_KEY);
                    RDelayedQueue<ShortLinkStatsRecordDTO> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
                    for (; ; ) {
                        try {
                            ShortLinkStatsRecordDTO statsRecord = delayedQueue.poll();
                            if (statsRecord != null) {
                                if (messageQueueIdempotentHandler.isMessageProcessed(statsRecord.getKeys())) {
                                    if(messageQueueIdempotentHandler.isAccomplish(statsRecord.getKeys())){
                                        return;
                                    }
                                    throw new ServiceException("消息未完成流程 需要消息队列充值");
                                }
                                try {
                                    shortLinkService.shortLinkStats(null, null, statsRecord);
                                }catch (Throwable ex){
                                    messageQueueIdempotentHandler.delMessageProcessed(statsRecord.getKeys());
                                    log.error("消费异常", ex);
                                }
                                continue;
                            }
                            LockSupport.parkUntil(500);
                        } catch (Throwable ignored) {
                        }
                    }
                });
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        onMessage();
    }
}
