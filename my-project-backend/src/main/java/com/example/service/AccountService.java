package com.example.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.entity.Account;
import com.example.domain.vo.request.EmailRegisterVo;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends IService<Account>, UserDetailsService {
    Account findAccountByNameOrEmail(String text);

    String registerEmailVerifyCode(String type,String email,String ip);

    String registerEmailAccount(EmailRegisterVo registerVo);
}

