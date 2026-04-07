package com.ginka.shortlink.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ginka.shortlink.shortlink.project.common.convention.exception.ClientException;
import com.ginka.shortlink.shortlink.project.common.convention.exception.ServiceException;
import com.ginka.shortlink.shortlink.project.common.enums.VailDateTypeEnum;
import com.ginka.shortlink.shortlink.project.dao.entity.ShortLinkDO;
import com.ginka.shortlink.shortlink.project.dao.mapper.LinkMapper;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.ginka.shortlink.shortlink.project.dto.resp.ShortLinkCountQueryRespDTO;
import com.ginka.shortlink.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.ginka.shortlink.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.ginka.shortlink.shortlink.project.service.ShortLinkService;
import com.ginka.shortlink.shortlink.project.toolkit.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @Override
    public List<ShortLinkCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
       return baseMapper.listGroupShortLinkCount(requestParam);
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        LambdaUpdateWrapper<ShortLinkDO> eq = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getOriginGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0)
                .set(Objects.equals(requestParam.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()), ShortLinkDO::getValidDate, null);
        ShortLinkDO shortLinkDO = baseMapper.selectOne(eq);
        if(shortLinkDO==null){
            throw new ServiceException("短链接不存在或短链接失效");
        }
        ShortLinkDO build = ShortLinkDO.builder().originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .fullShortUrl(requestParam.getFullShortUrl())
                .validDate(requestParam.getValidDate())
                .validDateType(requestParam.getValidDateType())
                .describe(requestParam.getDescribe())
                .build();
        if(requestParam.getGid().equals(requestParam.getOriginGid())){
            baseMapper.update(build, eq);
            //rBloomFilterConfiguration.add(requestParam.getFullShortUrl());
            return;
        }
        //在gid改变的情况下 删除原来的短链接 拼接查出来的短链接与传进来的修改参数 把新创建的短链接重新插入到数据库中
        ShortLinkDO build1 = ShortLinkDO.builder().id(shortLinkDO.getId())
                .fullShortUrl(requestParam.getFullShortUrl())
                .validDateType(requestParam.getValidDateType())
                .createdType(shortLinkDO.getCreatedType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .gid(requestParam.getGid())
                .domain(shortLinkDO.getDomain())
                .originUrl(requestParam.getOriginUrl())
                .shortUri(shortLinkDO.getShortUri())
                .favicon(shortLinkDO.getFavicon())
                .enableStatus(shortLinkDO.getEnableStatus())
                .clickNum(shortLinkDO.getClickNum())
                .createTime(shortLinkDO.getCreateTime())
                .build();
        ShortLinkDO.ShortLinkDOBuilder shortLinkDOBuilder = ShortLinkDO.builder().delFlag(1);
        baseMapper.update(shortLinkDOBuilder.build(), eq);
        baseMapper.insert(build1);

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
