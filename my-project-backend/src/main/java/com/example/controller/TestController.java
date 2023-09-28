package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: my-project
 * @description:
 * @author: 6420
 * @create: 2023-09-28 18:27
 **/
@RestController
@RequestMapping("/api/test")
public class TestController {
    @GetMapping("/hello")
    public String test(){

        return "Hello World!";
    }
}
