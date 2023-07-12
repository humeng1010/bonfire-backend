package com.usercenter.job;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.usercenter.constant.RedisConstant;
import com.usercenter.entity.User;
import com.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.usercenter.constant.RedisConstant.PROJECT_PREFIX_KEY;

@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private UserService userService;


    /**
     * 每天 23:59 执行这个定时任务进行缓存预热
     * 把每个用户的推荐列表缓存到 redis 中
     * 分布式的时候使用redis的setnx 防止多个服务器同时执行
     */
    @Scheduled(cron = "0 59 23 * * *")
    public void doCacheRecommendUser() {
        RLock lock = redissonClient.getLock(PROJECT_PREFIX_KEY + "precachejob:lock");
        try {
            //    更新redis
            log.info("缓存预热");
            // redisson看门狗机制 默认30s 会自动续期
            if (lock.tryLock(0, -1L, TimeUnit.MILLISECONDS)) {
                List<User> users = userService.list();
                for (User user : users) {
                    // 获取当前用户的第一页推荐用户的用户列表
                    IPage<User> userIPage = userService.recommendUsersById(user.getId(), 1L, 10L);
                    if (userIPage.getRecords().isEmpty()) {
                        continue;
                    }
                    // 信息脱敏
                    userIPage.setRecords(userIPage.getRecords()
                            .stream().map(userService::getSafetyUser).collect(Collectors.toList()));

                    String jsonStr = JSONUtil.toJsonStr(userIPage);
                    //    缓存到redis中
                    stringRedisTemplate.opsForValue().set(RedisConstant.USER_RECOMMEND_KEY + user.getId(), jsonStr);
                    log.info("{}用户推荐页缓存已更新", user.getId());
                }
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
