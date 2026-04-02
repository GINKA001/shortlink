package com.ginka.shortlink.shortlink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.ginka.shortlink.shortlink.admin.common.convention.exception.ClientException;
import com.ginka.shortlink.shortlink.admin.common.convention.result.Result;
import com.ginka.shortlink.shortlink.admin.common.convention.result.Results;
import com.ginka.shortlink.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.ginka.shortlink.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.ginka.shortlink.shortlink.admin.dto.resp.UserActualRespDTO;
import com.ginka.shortlink.shortlink.admin.dto.resp.UserRespDTO;
import com.ginka.shortlink.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/*
* 用户管理控制层
*/
@RestController()
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    /**
     * 根据用户名获取用户信息
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/api/short-link/v1/user/{username}")//@PathVariable 获取路径参数
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username) {
        UserRespDTO userRespDTO = userService.getUserByUsername(username);
        if (userRespDTO == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
        return Results.success(userRespDTO);
    }
    /**
     * 根据用户名获取用户信息
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/api/short-link/v1/actual/user/{username}")//@PathVariable 获取路径参数
    public Result<UserActualRespDTO> getActualUserByUsername(@PathVariable("username") String username) {
        UserRespDTO userRespDTO = userService.getUserByUsername(username);
        if (userRespDTO == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
        return Results.success(BeanUtil.toBean(userRespDTO, UserActualRespDTO.class));
    }
    /**
     * 判断用户名是否存在
     * @param username 用户名
     * @return true:存在 false:不存在
     */
    @GetMapping("/api/short-link/v1/user/has-username")
    public Result<Boolean> hasUserName(@RequestParam("username") String username) {
        return Results.success(!userService.hasUserName(username));
    }
    /**
     * 用户注册
     * @param requestParam 请求参数
     * @return null
     */

    @PostMapping("/api/short-link/v1/user")
    public Result<Void> register(@RequestBody UserRegisterReqDTO requestParam) {
        userService.register(requestParam);
        return Results.success();
    }
}
