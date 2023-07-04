package com.usercenter.service.impl;

import com.usercenter.common.BaseResponse;
import com.usercenter.entity.User;
import com.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Slf4j
class UserServiceImplTest {

    @Resource
    private UserService userService;

    @Test
    public void testSearchUsersByTag() {

        BaseResponse<List<User>> response = userService.searchAllUsersByTags(Arrays.asList("Java", "男"));
        Assertions.assertNotNull(response.getData());

        log.info("data:{}", response.getData());

    }

}