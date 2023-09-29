package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.domain.entity.Account;
import org.apache.ibatis.annotations.Mapper;


/**
 * (Account)表数据库访问层
 *
 * @author makejava
 * @since 2023-09-28 21:41:47
 */
@Mapper
public interface AccountMapper extends BaseMapper<Account> {

}
