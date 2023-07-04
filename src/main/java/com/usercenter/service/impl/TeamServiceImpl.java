package com.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usercenter.entity.Team;
import com.usercenter.mapper.TeamMapper;
import com.usercenter.service.TeamService;
import org.springframework.stereotype.Service;

/**
 * @author humeng
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2023-07-04 16:09:29
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

}




