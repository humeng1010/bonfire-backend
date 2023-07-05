package com.usercenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.usercenter.entity.Team;
import com.usercenter.entity.vo.TeamMemberVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author humeng
 * @description 针对表【team(队伍)】的数据库操作Mapper
 * @createDate 2023-07-04 16:09:29
 * @Entity generator.domain.Team
 */
@Mapper
public interface TeamMapper extends BaseMapper<Team> {
    List<TeamMemberVO> selectTeamWithMemberInfo(@Param("id") Integer id);

}




