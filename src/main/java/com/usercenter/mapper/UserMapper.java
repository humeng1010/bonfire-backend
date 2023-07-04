package com.usercenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.usercenter.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author humeng
 * @description 针对表【user(用户表)】的数据库操作Mapper
 * @createDate 2023-06-10 13:51:14
 * @Entity generator.entity.User
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




