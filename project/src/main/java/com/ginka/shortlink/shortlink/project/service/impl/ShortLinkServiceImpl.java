package com.ginka.shortlink.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ginka.shortlink.shortlink.project.common.convention.exception.ClientException;
import com.ginka.shortlink.shortlink.project.common.convention.exception.ServiceException;
import com.ginka.shortlink.shortlink.project.dao.entity.ShortLinkDO;
import com.ginka.shortlink.shortlink.project.dao.mapper.LinkMapper;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.ginka.shortlink.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.ginka.shortlink.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.ginka.shortlink.shortlink.project.service.ShortLinkService;
import com.ginka.shortlink.shortlink.project.toolkit.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<LinkMapper, ShortLinkDO> implements ShortLinkService {
    private final RBloomFilter<String> rBloomFilterConfiguration;
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        //生成后缀
        String shortLinkSuffix = generateSuffix(requestParam);
        ShortLinkDO shortLinkDO = BeanUtil.toBean(requestParam, ShortLinkDO.class);
        shortLinkDO.setShortUri(shortLinkSuffix);
        shortLinkDO.setEnableStatus(0);
        // 拼接完整短链接
        shortLinkDO.setFullShortUrl(requestParam.getDomain()+"/"+shortLinkSuffix);

        //布隆过滤器后防止漏判
        try {
            baseMapper.insert(shortLinkDO);
        }catch (DuplicateKeyException e){
            //todo 对误判的短链接怎么处理 1.存在于缓存 2.不存在于缓存 存在则抛异常 不存在则将其添加到布隆过滤器之中
            LambdaQueryWrapper<ShortLinkDO> eq = Wrappers.lambdaQuery(ShortLinkDO.class).eq(ShortLinkDO::getFullShortUrl, shortLinkDO.getFullShortUrl());
            ShortLinkDO shortLinkDO1 = baseMapper.selectOne(eq);
            if(shortLinkDO1!=null) {
                log.warn("短链接: {} 重复入库", shortLinkDO.getFullShortUrl());
                throw new ServiceException("短链接已存在");
            }
        }
        rBloomFilterConfiguration.add(shortLinkDO.getFullShortUrl());
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .build();
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> eq = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0)
                .orderByDesc(ShortLinkDO::getCreateTime);
        IPage<ShortLinkDO> page0 = baseMapper.selectPage(requestParam, eq);
        //将查询的DO转化为响应参数
        IPage<ShortLinkPageRespDTO> convert1 = page0.convert(item -> BeanUtil.toBean(item, ShortLinkPageRespDTO.class));
        return convert1;
    }

    // 生成短链接后缀  添加布隆过滤器 避免缓存穿透
    private String generateSuffix(ShortLinkCreateReqDTO requestParam) {
        String shortUri;
        //发生冲突时的尝试次数
        int customGenerateCount = 0;
        String originUrl = requestParam.getOriginUrl();
        while(true) {
            if(customGenerateCount>=10) {
                throw new ClientException("短链接生成失败");
            }
            shortUri = HashUtil.hashToBase62(originUrl);
            if(!rBloomFilterConfiguration.contains(requestParam.getDomain()+"/"+shortUri)) {
                break;
            }
            originUrl+=System.currentTimeMillis();
            customGenerateCount++;
        }
        return shortUri;
    }
}
