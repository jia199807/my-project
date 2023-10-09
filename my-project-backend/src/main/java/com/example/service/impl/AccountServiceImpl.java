package com.example.service.impl;



import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.domain.entity.Account;
import com.example.domain.vo.request.EmailRegisterVo;
import com.example.mapper.AccountMapper;
import com.example.service.AccountService;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Wrapper;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 账户信息处理相关服务
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Resource
    FlowUtils flowUtils;


    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    AmqpTemplate amqpTemplate;

    @Resource
    PasswordEncoder passwordEncoder;


    /**
     * 从数据库中通过用户名或邮箱查找用户详细信息
     * @param username 用户名
     * @return 用户详细信息
     * @throws UsernameNotFoundException 如果用户未找到则抛出此异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {


        Account account = this.findAccountByNameOrEmail(username);
        if(account == null)
            throw new UsernameNotFoundException("用户名或密码错误");
        return User
                .withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }
    /**
     * 通过用户名或邮件地址查找用户
     * @param text 用户名或邮件
     * @return 账户实体
     */
    public Account findAccountByNameOrEmail(String text){
        return this.query()
                .eq("username", text).or()
                .eq("email", text)
                .one();
    }

    @Override
    public String registerEmailVerifyCode(String type, String email, String ip) {
        synchronized (ip.intern()){
            if (!verifyLimit(ip)){
                return "请求频繁，请稍后再试";
            }

            // 生成验证码
            Random random = new Random();
            int code = random.nextInt(899999) + 100000;
            Map<String, Object> data=Map.of("type",type,"email",email,"code",code);
            // 放入消息队列
            amqpTemplate.convertAndSend("mail",data);
            // 放入redis
            stringRedisTemplate.opsForValue()
                    .set(Const.VERIFY_EMAIL_DATA+email,String.valueOf(code),3, TimeUnit.MINUTES);

            return null;
        }


    }

    @Override
    public String registerEmailAccount(EmailRegisterVo registerVo) {
        String email=registerVo.getEmail();
        String username = registerVo.getUsername();
        String key = Const.VERIFY_EMAIL_DATA + email;
        String code= stringRedisTemplate.opsForValue().get(key);
        if(code==null)
            return "请先获取验证码";
        if(!code.equals(registerVo.getCode()))
            return "验证码输入错误，请重新输入";
        if (existAccountByEmail(email))
            return "此邮箱已被注册";
        if (existAccountByUsername(username))
            return "此用户名已被注册";

        String password = passwordEncoder.encode(registerVo.getPassword());
        Account user = new Account(null, username, password, email, "user", new Date());
        if(save(user)) {
            stringRedisTemplate.delete(key);
            return null;
        }
        else
            return "内部错误，请联系管理员";
    }

    private boolean existAccountByEmail(String email){
        return this.baseMapper.exists(lambdaQuery().eq(Account::getEmail,email).getWrapper());
    }
    private boolean existAccountByUsername(String username){
        return this.baseMapper.exists(lambdaQuery().eq(Account::getUsername,username).getWrapper());
    }

    /**
     * @author 6420
     * @description 验证码限流
     * @date 19:32 2023/10/5
     * @param ip
     * @return boolean
     **/
    private boolean verifyLimit(String ip){
        String key = Const.VERIFY_EMAIL_LIMIT + ip;
        return flowUtils.limitOnceCheck(key,60);

    }

}