package com.ginka.shortlink.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.MD5;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ginka.shortlink.shortlink.admin.common.constant.RedisCacheConstant;
import com.ginka.shortlink.shortlink.admin.common.convention.exception.ClientException;
import com.ginka.shortlink.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.ginka.shortlink.shortlink.admin.dao.entity.UserDO;
import com.ginka.shortlink.shortlink.admin.dao.mapper.UserMapper;
import com.ginka.shortlink.shortlink.admin.dto.req.UserLoginReqDTO;
import com.ginka.shortlink.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.ginka.shortlink.shortlink.admin.dto.req.UserUpdateDTO;
import com.ginka.shortlink.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.ginka.shortlink.shortlink.admin.dto.resp.UserRespDTO;
import com.ginka.shortlink.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 */
@RequiredArgsConstructor
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;// RedissonClient分布式锁
    private final StringRedisTemplate stringRedisTemplate;
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
        RLock lock = redissonClient.getLock(RedisCacheConstant.LOCK_USER_REGISTER_KEY);
        /*
         * 应对并发恶意请求
         * 尝试获取锁
         * 获取锁成功 继续执行业务逻辑
         * 获取锁失败 抛出异常
         * 释放锁
         */
        try {
            if(lock.tryLock()) {
                int insert = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
                if (insert < 1) {
                    throw new ClientException(UserErrorCodeEnum.USER_SAVE_ERROR);
                }
                userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
                return;
            }
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void update(UserUpdateDTO requestParam) {
        //ToDO 验证当前用户是否登录
        LambdaQueryWrapper<UserDO> eq = Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getUsername, requestParam.getUsername());
        baseMapper.update(BeanUtil.toBean(requestParam, UserDO.class), eq);
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        LambdaQueryWrapper<UserDO> eq = Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getUsername, requestParam.getUsername()).eq(UserDO::getPassword, requestParam.getPassword()).eq(UserDO::getDelFlag,0);
        UserDO userDO = baseMapper.selectOne(eq);
        if(userDO==null){
            throw new ClientException("用户不存在");
        }
        Boolean hasLogin = stringRedisTemplate.hasKey("login_"+requestParam.getUsername());
        if(hasLogin!=null && hasLogin){
            throw new ClientException("用户已登录");
        }
        /**
         *
         * 生成token
         */
        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForValue().set(uuid,JSON.toJSONString(userDO),30L, TimeUnit.DAYS);
        //存储用户信息
        Map<String,Object> userInfoMap=new HashMap<>();
        userInfoMap.put("token",JSON.toJSONString(userDO));
        //
        stringRedisTemplate.opsForHash().put("login_"+requestParam.getUsername(),uuid,JSON.toJSONString(userDO));
        stringRedisTemplate.expire("login_"+requestParam.getUsername(),30L,TimeUnit.DAYS);

        return new UserLoginRespDTO(uuid);
    }

    @Override
    public Boolean checkLogin(String username,String token) {
        Object tokenValue = stringRedisTemplate.opsForHash().get("login_" + username, token);//获取用户信息
        return tokenValue != null;

    }

    @Override
    public void logout(String username, String token) {
        if(!checkLogin(username,token)){
            throw new ClientException("用户未登录,无法退出登录");
        }
        stringRedisTemplate.delete("login_" + username);
    }
}
