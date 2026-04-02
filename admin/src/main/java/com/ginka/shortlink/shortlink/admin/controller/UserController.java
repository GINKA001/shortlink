package com.ginka.shortlink.shortlink.admin.controller;

import com.ginka.shortlink.shortlink.admin.common.convention.result.Result;
import com.ginka.shortlink.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.ginka.shortlink.shortlink.admin.dto.resp.UserRespDTO;
import com.ginka.shortlink.shortlink.admin.service.UserService;

import lombok.RequiredArgsConstructor;
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
            return new Result<UserRespDTO>().setCode(UserErrorCodeEnum.USER_NULL.code()).setMessage(UserErrorCodeEnum.USER_NULL.message());
        }
        Result<UserRespDTO> result = new Result<>();
        result.setCode("0");
        result.setData(userRespDTO);
        return result;
    }
}
