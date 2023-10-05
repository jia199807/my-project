package com.example.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @program: my-project
 * @description:
 * @author: 6420
 * @create: 2023-10-05 19:21
 **/
@Component
public class FlowUtils {
    @Resource
    StringRedisTemplate template;
    public boolean limitOnceCheck(String key,int blockTime){
        if (template.hasKey(key)){
            return false;
        }else {
            template.opsForValue().set(key,"",blockTime, TimeUnit.SECONDS);
            return true;
        }

    }
}
