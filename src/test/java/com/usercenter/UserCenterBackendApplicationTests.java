package com.usercenter;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.secure.SaSecureUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
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
}
