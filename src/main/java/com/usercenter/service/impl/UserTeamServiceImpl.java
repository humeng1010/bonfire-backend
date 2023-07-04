package com.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usercenter.entity.UserTeam;
import com.usercenter.mapper.UserTeamMapper;
import com.usercenter.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
 * @author humeng
 * @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
 * @createDate 2023-07-04 16:12:27
 */
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
        implements UserTeamService {

}




