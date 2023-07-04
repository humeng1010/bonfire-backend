package com.usercenter.once;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.hutool.core.util.IdUtil;
import com.usercenter.entity.User;
import com.usercenter.mapper.UserMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

import static com.usercenter.constant.UserConstant.SALT;

@Component
public class InsertUsers {
    @Resource
    private UserMapper userMapper;

    /**
     * 批量插入用户
     * 启用定时任务,只执行一次
     */
    // @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 一千
        final int INSERT_NUM = 1000;
        for (int i = 0; i < INSERT_NUM; i++) {

            // 对密码进行加盐加密
            String encryptPassword = SaSecureUtil.md5(SALT + "12345678");

            // 插入数据
            User user = new User();
            user.setUsername("momo_" + IdUtil.nanoId(5));
            user.setAvatarUrl("https://img.alicdn.com/bao/uploaded/i1/232692832/O1CN01XERLVq1Wn6Sq5ufB4_!!232692832.jpg_400x400q90");
            user.setUserAccount("momo_" + IdUtil.nanoId(5));
            user.setUserPassword(encryptPassword);

            user.setGender(0);
            user.setPhone("17777777777");
            user.setEmail("test@test.com");
            user.setTags("[\"JAVA\",\"男\",\"入职\",\"单身\"]");
            user.setProfile("测试数据:" + i);

            userMapper.insert(user);
        }
        stopWatch.stop();

        System.out.println(stopWatch.getTotalTimeMillis());

    }
}
