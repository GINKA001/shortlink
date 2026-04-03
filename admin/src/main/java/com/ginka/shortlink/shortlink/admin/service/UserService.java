package com.ginka.shortlink.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ginka.shortlink.shortlink.admin.dao.entity.UserDO;
import com.ginka.shortlink.shortlink.admin.dto.req.UserLoginReqDTO;
import com.ginka.shortlink.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.ginka.shortlink.shortlink.admin.dto.req.UserUpdateDTO;
import com.ginka.shortlink.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.ginka.shortlink.shortlink.admin.dto.resp.UserRespDTO;

public interface UserService extends IService<UserDO> {
    /**
     * 根据用户名获取用户信息
     * @param username 用户名
     * @return 用户实体类信息
     */
    UserRespDTO getUserByUsername(String username);
    /**
     * 判断用户名是否存在
     * @param username 用户名
     * @return true:存在 false:不存在
     */
    Boolean hasUserName(String username);
/**
 * 用户注册
 * @param requestParam 注册参数
 */
    void register(UserRegisterReqDTO requestParam);
    /**
     * 用户更新
     * @param requestParam 更新参数
     */
    void update(UserUpdateDTO requestParam);
    /**
     * 用户登录
     * @param requestParam 用户登录请求体
     * @return token
     */
    UserLoginRespDTO login(UserLoginReqDTO requestParam);
    /**
     * 验证用户登录
     * @param token token
     * @param username 用户名
     * @return true:登录成功 false:登录失败
     */
    Boolean checkLogin(String username,String token);

    /**
     * 用户登出
     * @param token token
     * @param username 用户名
     * @return null
     */
    void logout(String username,String token);
}
