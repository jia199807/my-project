package com.example.vo.response;

import lombok.Data;

import java.util.Date;

/**
 * @program: my-project
 * @description: 认证VO
 * @author: 6420
 * @create: 2023-09-25 23:21
 **/
@Data
public class AuthorizeVO {
    String username;
    String role;
    String token;
    Date expire;
}
