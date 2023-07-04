package com.usercenter;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    public void testRedisson() {

        // 保存在内存中
        List<String> list = new ArrayList<>();
        list.add("xiaohu");
        System.out.println(list.get(0));
        // list.remove(0);

        // 保存在redis中
        // 指定key
        RList<String> rList = redissonClient.getList("test-list");
        rList.add("redisson");
        System.out.println(rList.get(0));
        rList.remove(0);

    }
}
