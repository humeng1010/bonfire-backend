package com.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.usercenter.common.BaseResponse;
import com.usercenter.entity.Team;
import com.usercenter.entity.User;

/**
 * @author humeng
 * @description 针对表【team(队伍)】的数据库操作Service
 * @createDate 2023-07-04 16:09:29
 */
public interface TeamService extends IService<Team> {


    BaseResponse<Long> addTeam(Team team, User user);
}
