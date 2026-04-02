package com.ginka.shortlink.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ginka.shortlink.shortlink.admin.common.convention.exception.ClientException;
import com.ginka.shortlink.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.ginka.shortlink.shortlink.admin.dao.entity.UserDO;
import com.ginka.shortlink.shortlink.admin.dao.mapper.UserMapper;
import com.ginka.shortlink.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.ginka.shortlink.shortlink.admin.dto.resp.UserRespDTO;
import com.ginka.shortlink.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */
@RequiredArgsConstructor
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> eq = Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getUsername, username);
        UserDO userByUsername = baseMapper.selectOne(eq);
        if (userByUsername == null)
            return null;
        UserRespDTO userRespDTO = new UserRespDTO();
        BeanUtils.copyProperties(userByUsername, userRespDTO);
        return userRespDTO;
    }
/**
 * 根据用户名判断用户名是否存在
 * @param username  用户名
 * @return  True:存在  False:不存在
 */
    @Override
    public Boolean hasUserName(String username) {
        //mybatis-plus实现方式
//        LambdaQueryWrapper<UserDO> eq = Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getUsername, username);
//        UserDO userDO = baseMapper.selectOne(eq);
//        return userDO==null;
        //布隆过滤器实现方式
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }
    /**
     * 用户注册
     * @param requestParam  用户注册请求参数
     */
    @Override
    public void register(UserRegisterReqDTO requestParam) {
        if(hasUserName(requestParam.getUsername())){
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
        }
        int insert = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
        if (insert < 1) {
            throw new ClientException(UserErrorCodeEnum.USER_SAVE_ERROR);
        }
        userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
    }
}
