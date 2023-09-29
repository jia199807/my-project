package com.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @program: my-project
 * @description: Jwt工具类
 * @author: 6420
 * @create: 2023-09-25 23:05
 **/
@Component
public class JwtUtils {
    @Value("${spring.security.jwt.key}")
    String key;

    @Value("${spring.security.jwt.expire}")
    int expire;

    @Resource
    StringRedisTemplate template;

    public boolean invalidateJwt(String headerToken){
        String token=this.convertToken(headerToken);
        if (token==null)
            return false;
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT jwt = jwtVerifier.verify(token);
            String id = jwt.getId();
            return deleteToken(id,jwt.getExpiresAt());
        }catch (JWTVerificationException e){
            return false;
        }

    }

    private boolean deleteToken(String uuid,Date time ){
        if(this.isInvalidToken(uuid))
            return false;
        Date now = new Date();
        long expire= Math.max(time.getTime()-now.getTime(),0);
        template.opsForValue().set(Const.JWT_BLACK_LIST+uuid,"",expire, TimeUnit.MILLISECONDS );
        return true;
    }

    private boolean isInvalidToken(String uuid){
       return Boolean.TRUE.equals(template.hasKey(Const.JWT_BLACK_LIST + uuid));
    }


    public DecodedJWT resolveJwt(String headerToken){
        // 判空
        String token = this.convertToken(headerToken);
        if (token==null)
            return null;

        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            // 验证是否篡改过
            DecodedJWT verify = jwtVerifier.verify(token);
            // 验证是否失效
            if (this.isInvalidToken(verify.getId()))
                return null;

            // 验证是否过期
            Date expiresAt = verify.getExpiresAt();
            return new Date().after(expiresAt) ? null:verify;

        } catch (JWTVerificationException e) {
            return null;
        }
    }

    /**
     * 根据UserDetails生成对应的Jwt令牌
     * @param user 用户信息
     * @return 令牌
     */
    public String createJwt(UserDetails user, String username, int userId) {
            Algorithm algorithm = Algorithm.HMAC256(key);
            Date expire = this.expireTime();
            return JWT.create()
                    .withJWTId(UUID.randomUUID().toString())
                    .withClaim("id", userId)
                    .withClaim("name", username)
                    .withClaim("authorities", user.getAuthorities()
                            .stream()
                            .map(GrantedAuthority::getAuthority).toList())
                    .withExpiresAt(expire)
                    .withIssuedAt(new Date())
                    .sign(algorithm);
    }

    public Date expireTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR,expire*24);
        return calendar.getTime();
    }

    public UserDetails toUser(DecodedJWT jwt){
        Map<String, Claim> claims = jwt.getClaims();
        return User
                .withUsername(claims.get("name").asString())
                .password("*****")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }

    public Integer toId(DecodedJWT jwt){
        Map<String, Claim> claims = jwt.getClaims();
        return claims.get("id").asInt();
    }

    public String convertToken(String headerToken){
        if (headerToken==null||!headerToken.startsWith("Bearer "))
            return null;
        return headerToken.substring(7);
    }

}
