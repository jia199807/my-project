package com.example.entity;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

/**
 * @program: my-project
 * @description: 响应实体类
 * @author: 6420
 * @create: 2023-09-25 22:45
 **/
public record RestBean<T>(int code,T data,String message) {

    public static <T> RestBean<T> success(T data){
        return new RestBean<>(200,data,"请求成功");
    }
    public static <T> RestBean<T> success(){
        return success(null);
    }

    public String asJsonString(){
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteNulls);
    }

    public static <T> RestBean<T> failure(int code,String message){
        return new RestBean<>(code,null,message);
    }
}
