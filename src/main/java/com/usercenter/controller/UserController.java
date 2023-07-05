package com.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.usercenter.common.BaseResponse;
import com.usercenter.common.ErrorCode;
import com.usercenter.constant.UserConstant;
import com.usercenter.entity.User;
import com.usercenter.entity.request.UserLoginRequest;
import com.usercenter.entity.request.UserRegisterRequest;
import com.usercenter.exception.BusinessException;
import com.usercenter.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

import static com.usercenter.constant.UserConstant.USER_LOGIN_STATUS;

/**
 * 用户接口
 *
 * @author humeng
 */
@RestController
@RequestMapping("/user")
@Api(tags = "用户相关接口")
// @CrossOrigin // 上线通过 nginx 进行反向代理解决跨域
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private HttpServletRequest httpServletRequest;

    @ApiOperation("注册新用户")
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (Objects.isNull(userRegisterRequest))
            throw new BusinessException(ErrorCode.NULL_ERROR);

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword))
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        return userService.userRegister(userAccount, userPassword, checkPassword);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest) {
        if (Objects.isNull(userLoginRequest))
            throw new BusinessException(ErrorCode.NULL_ERROR);

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword))
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        return userService.doLogin(userAccount, userPassword);
    }

    @PostMapping("/out-login")
    public BaseResponse<String> userOutLogin() {
        httpServletRequest.getSession().removeAttribute(USER_LOGIN_STATUS);
        return BaseResponse.ok("ok");
    }

    /**
     * 获取当前登陆的用户
     *
     * @return 登陆的用户
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentLoginUser() {
        User sessionCacheUser = (User) httpServletRequest.getSession().getAttribute(USER_LOGIN_STATUS);
        if (Objects.isNull(sessionCacheUser)) throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        // 防止数据库中的用户信息改变了,session缓存中的用户信息没有改变,造成的数据缓存不一致
        User databaseUser = userService.getById(sessionCacheUser.getId());
        User safetyUser = userService.getSafetyUser(databaseUser);
        return BaseResponse.ok(safetyUser);
    }

    @GetMapping("/search")
    // current=1&pageSize=5
    public BaseResponse<IPage<User>> searchUsers(
            @RequestParam("current") Long current,
            @RequestParam("pageSize") Long pageSize,
            @RequestParam(value = "username", required = false) String username) {

        if (!isAdmin()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        return userService.searchUsers(current, pageSize, username);
    }


    @DeleteMapping("/{id}")
    public BaseResponse<String> deleteUserById(@PathVariable("id") Long id) {
        if (!isAdmin()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        if (id <= 0) return BaseResponse.error(ErrorCode.PARAMS_ERROR);
        boolean remove = userService.removeById(id);
        if (!remove) {
            throw new BusinessException(ErrorCode.SERVICE_ERROR);
        }
        return BaseResponse.ok("删除成功");
    }

    @PutMapping("/{id}")
    public BaseResponse<String> changeUserStatus(@PathVariable("id") Long id) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .set(id != null, User::getUserStatus, 1)
                .eq(id != null, User::getId, id);
        boolean update = userService.update(updateWrapper);
        if (!update) {
            return BaseResponse.error(50020, "禁用失败");
        }
        return BaseResponse.ok("禁用成功");

    }

    @PutMapping("/enable/{id}")
    public BaseResponse<String> enableUserStatusById(@PathVariable("id") Long id) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .set(id != null, User::getUserStatus, 0)
                .eq(id != null, User::getId, id);
        boolean update = userService.update(updateWrapper);
        if (!update) {
            return BaseResponse.error(50020, "启用失败");
        }
        return BaseResponse.ok("启用成功");

    }

    @PutMapping("/enable-admin/{id}")
    public BaseResponse<String> enableAdminRoleById(@PathVariable("id") Long id) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .set(id != null, User::getUserRole, 1)
                .eq(id != null, User::getId, id);
        boolean update = userService.update(updateWrapper);
        if (!update) {
            return BaseResponse.error(50020, "授权失败");
        }
        return BaseResponse.ok("授权成功");

    }


    @PutMapping("/update")
    public BaseResponse<String> updateUserById(@RequestBody User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        return userService.updateUser(user);
    }

    @GetMapping("/recommend")
    public BaseResponse<IPage<User>> recommendUsers(
            @RequestParam(value = "currentPage", defaultValue = "1", required = false) Long currentPage,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Long pageSize
    ) {
        return userService.recommendUsers(currentPage, pageSize);
    }

    @GetMapping("/searchUsersByTagsWithPage")
    public BaseResponse<IPage<User>> searchUsersByTags(
            @RequestParam(value = "currentPage", defaultValue = "1", required = false) Long currentPage,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Long pageSize,
            @RequestParam(value = "tags") List<String> tags) {
        return userService.searchUsersByTags(currentPage, pageSize, tags);
    }

    @GetMapping("/searchUsersByTags")
    public BaseResponse<List<User>> searchAllUsersByTags(@RequestParam(value = "tags") List<String> tags) {
        return userService.searchAllUsersByTags(tags);
    }

    /**
     * 判断用户是否是管理员
     *
     * @return true: admin user; false: other user
     */
    public Boolean isAdmin() {
        User user = (User) httpServletRequest.getSession().getAttribute(USER_LOGIN_STATUS);
        return user != null && Objects.equals(user.getUserRole(), UserConstant.ADMIN_ROLE);
    }


}
