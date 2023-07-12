package com.usercenter;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.secure.SaSecureUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.usercenter.constant.RedisConstant;
import com.usercenter.entity.User;
import com.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@Slf4j
class UserCenterBackendApplicationTests {

    @Test
    void contextLoads() {

        String s = SaSecureUtil.md5("123456");
        System.out.println(s);

        System.out.println(SaSecureUtil.sha1("123456"));

        System.out.println(SaSecureUtil.sha256("123456"));
    }

    @Test
    void bcrypt() {
        // gensalt方法提供了可选参数 (log_rounds) 来定义加盐多少，也决定了加密的复杂度:
        // 使用方法
        String pw_hash = BCrypt.hashpw("123456", BCrypt.gensalt(10));

        // 使用checkpw方法检查被加密的字符串是否与原始字符串匹配：
        System.out.println(BCrypt.checkpw("123456", pw_hash));


    }


    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testCachePreHot() {
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
}
