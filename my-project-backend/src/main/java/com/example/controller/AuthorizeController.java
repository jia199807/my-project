package com.example.controller;

import com.example.domain.RestBean;
import com.example.domain.vo.request.EmailRegisterVo;
import com.example.service.AccountService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Supplier;

/**
 * @program: my-project
 * @description:
 * @author: 6420
 * @create: 2023-10-05 19:54
 **/

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {
    @Resource
    AccountService accountService;
    @GetMapping("/ask-code")
    public RestBean<Void> askVerifyCode(@RequestParam @Pattern(regexp ="(register|reset)") String type,
                                        @RequestParam @Email String email,
                                        HttpServletRequest request) {
        return messageHandle(()->accountService.registerEmailVerifyCode(type, email, request.getRemoteAddr()));
    }
    @PostMapping("register")
    public RestBean<Void> register(@RequestBody @Valid EmailRegisterVo registerVo){
        return messageHandle(()->accountService.registerEmailAccount(registerVo));
    }

    private RestBean<Void> messageHandle(Supplier<String> action){
        String message = action.get();
        return message == null ? RestBean.success(): RestBean.failure(400,message);
    }
}
