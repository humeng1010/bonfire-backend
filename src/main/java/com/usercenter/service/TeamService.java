package com.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.usercenter.common.BaseResponse;
import com.usercenter.entity.Team;
import com.usercenter.entity.User;
import com.usercenter.entity.dto.TeamQuery;
import com.usercenter.entity.request.TeamJoinRequest;
import com.usercenter.entity.request.TeamQuitRequest;
import com.usercenter.entity.request.TeamUpdateRequest;
import com.usercenter.entity.vo.TeamUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author humeng
 * @description 针对表【team(队伍)】的数据库操作Service
 * @createDate 2023-07-04 16:09:29
 */
public interface TeamService extends IService<Team> {


    BaseResponse<Long> addTeam(Team team, User user);

    /**
     * 搜索队伍
     *
     * @param teamQuery
     * @return
     */
    BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, User loginUser);

    BaseResponse<Boolean> updateTeam(TeamUpdateRequest teamUpdateRequest);

    /**
     * 加入队伍
     *
     * @param teamJoinRequest
     * @param httpServletRequest
     * @return
     */
    BaseResponse<Boolean> joinTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest httpServletRequest);

    BaseResponse<Boolean> quitTeam(TeamQuitRequest teamQuitRequest, HttpServletRequest httpServletRequest);

}
