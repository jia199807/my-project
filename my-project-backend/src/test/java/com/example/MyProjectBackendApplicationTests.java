package com.example;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.example.domain.entity.Account;
import com.example.service.AccountService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class MyProjectBackendApplicationTests {
    @Resource
    AccountService accountService;

    @Test
    void contextLoads() {
        System.out.println(new BCryptPasswordEncoder().encode("123456"));
    }

    @Test
    void name() {
        String email = "642096532@qq.com" ;
        System.out.println(accountService.getBaseMapper().exists(accountService.lambdaQuery().eq(Account::getEmail, email).getWrapper()));
    }
}
