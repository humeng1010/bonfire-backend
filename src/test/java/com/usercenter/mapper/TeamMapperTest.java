package com.usercenter.mapper;

import com.usercenter.entity.vo.TeamMemberVO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class TeamMapperTest {

    @Resource
    private TeamMapper teamMapper;

    @Test
    public void testSelectTeamMemberInfo() {
        List<TeamMemberVO> teamMemberVOS = teamMapper.selectTeamWithMemberInfo(3);
        System.out.println(teamMemberVOS);

    }

}