package com.usercenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.usercenter.entity.UserTeam;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author humeng
 * @description 针对表【user_team(用户队伍关系)】的数据库操作Mapper
 * @createDate 2023-07-04 16:12:27
 * @Entity generator.domain.UserTeam
 */
@Mapper
public interface UserTeamMapper extends BaseMapper<UserTeam> {

}




