package com.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.usercenter.common.BaseResponse;
import com.usercenter.common.ErrorCode;
import com.usercenter.entity.Team;
import com.usercenter.entity.User;
import com.usercenter.entity.dto.TeamQuery;
import com.usercenter.entity.request.TeamAddRequest;
import com.usercenter.entity.request.TeamJoinRequest;
import com.usercenter.entity.request.TeamQuitRequest;
import com.usercenter.entity.request.TeamUpdateRequest;
import com.usercenter.entity.vo.TeamUserInfoVO;
import com.usercenter.entity.vo.TeamUserVO;
import com.usercenter.exception.BusinessException;
import com.usercenter.service.TeamService;
import com.usercenter.service.UserService;
import io.swagger.annotations.Api;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 组队接口
 *
 * @author humeng
 */
@RestController
@RequestMapping("/team")
@Api(tags = "组队相关接口")
// @CrossOrigin // 上线通过 nginx 进行反向代理解决跨域
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private HttpServletRequest httpServletRequest;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前用户
        User loginUser = userService.getLoginUser(httpServletRequest);

        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        return teamService.addTeam(team, loginUser);
    }

    /**
     * 删除队伍
     *
     * @param teamId
     * @return
     */
    @DeleteMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody long teamId) {
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean del = teamService.deleteTeam(teamId);
        if (!del) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return BaseResponse.ok(true);
    }


    @PutMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        return teamService.updateTeam(teamUpdateRequest);

    }


    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(@RequestParam("id") long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);

        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return BaseResponse.ok(team, "根据id查询成功");

    }

    @GetMapping("/get-by-id")
    public BaseResponse<List<TeamUserInfoVO>> getTeamByLoginId() {
        User loginUser = userService.getLoginUser(httpServletRequest);

        return teamService.getTeamByCurrentUserId(loginUser.getId());
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        return teamService.joinTeam(teamJoinRequest, httpServletRequest);
    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return teamService.quitTeam(teamQuitRequest, httpServletRequest);
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> getListTeam(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return teamService.listTeams(teamQuery, loginUser);
    }

    @GetMapping("/list/page")
    public BaseResponse<IPage<Team>> getTeamByPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);

        Long currentPage = teamQuery.getCurrentPage();
        if (currentPage == null) currentPage = 1L;

        Long pageSize = teamQuery.getPageSize();
        if (pageSize == null) pageSize = 10L;

        IPage<Team> teamPage = new Page<>(currentPage, pageSize);

        LambdaQueryWrapper<Team> teamLambdaQueryWrapper = new LambdaQueryWrapper<>(team);
        teamService.page(teamPage, teamLambdaQueryWrapper);

        return BaseResponse.ok(teamPage);

    }

}
