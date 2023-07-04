package com.usercenter.job;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static com.usercenter.constant.RedisConstant.PROJECT_PREFIX_KEY;

@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;


    /**
     * 每天 23:59 执行这个定时任务进行缓存预热
     * 把每个用户的推荐列表缓存到 redis 中
     * 分布式的时候使用redis的setnx 防止多个服务器同时执行
     */
    @Scheduled(cron = "0 59 23 * * *")
    public void doCacheRecommendUser() {
        RLock lock = redissonClient.getLock(PROJECT_PREFIX_KEY + "precachejob:lock");
        try {
            // redisson看门狗机制 默认30s 会自动续期
            if (lock.tryLock(0, -1L, TimeUnit.MILLISECONDS)) {

                //    更新redis
                log.info("缓存预热");


            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }

        }


    }
}
