package com.ginka.shortlink.shortlink.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ginka.shortlink.shortlink.project.common.constant.RedisKeyConstant;
import com.ginka.shortlink.shortlink.project.dao.entity.ShortLinkDO;
import com.ginka.shortlink.shortlink.project.dao.mapper.LinkMapper;
import com.ginka.shortlink.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.ginka.shortlink.shortlink.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl extends ServiceImpl<LinkMapper,ShortLinkDO> implements RecycleBinService {
    private final StringRedisTemplate stringRedisTemplate;
    @Override
    public void saveRecycleBin(RecycleBinSaveReqDTO requestParam) {
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class).eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0);
        baseMapper.update(ShortLinkDO.builder().enableStatus(1).build(), updateWrapper);
        stringRedisTemplate.delete(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
    }
}
