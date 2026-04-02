package com.ginka.shortlink.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ginka.shortlink.shortlink.admin.dao.entity.UserDO;
import com.ginka.shortlink.shortlink.admin.dto.resp.UserRespDTO;

public interface UserService extends IService<UserDO> {
    /**
     * 根据用户名获取用户信息
     * @param username
     * @return 用户实体类信息
     */
    UserRespDTO getUserByUsername(String username);
}
