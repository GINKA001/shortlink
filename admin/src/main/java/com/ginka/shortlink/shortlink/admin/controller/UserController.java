package com.ginka.shortlink.shortlink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.ginka.shortlink.shortlink.admin.common.convention.exception.ClientException;
import com.ginka.shortlink.shortlink.admin.common.convention.result.Result;
import com.ginka.shortlink.shortlink.admin.common.convention.result.Results;
import com.ginka.shortlink.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.ginka.shortlink.shortlink.admin.common.web.GlobalExceptionHandler;
import com.ginka.shortlink.shortlink.admin.dto.resp.UserActualRespDTO;
import com.ginka.shortlink.shortlink.admin.dto.resp.UserRespDTO;
import com.ginka.shortlink.shortlink.admin.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
/*
* 用户管理控制层
*/
@RestController()
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    /**
     * 根据用户名获取用户信息
     * @param username
     * @return
     */
    @GetMapping("/api/shortlink/v1/user/{username}")//@PathVariable 获取路径参数
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username) {
        UserRespDTO userRespDTO = userService.getUserByUsername(username);
        if (userRespDTO == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
        return Results.success(userRespDTO);
    }
    @GetMapping("/api/shortlink/v1/actual/user/{username}")//@PathVariable 获取路径参数
    public Result<UserActualRespDTO> getActualUserByUsername(@PathVariable("username") String username) {
        UserRespDTO userRespDTO = userService.getUserByUsername(username);
        if (userRespDTO == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
        return Results.success(BeanUtil.toBean(userRespDTO, UserActualRespDTO.class));
    }
}
