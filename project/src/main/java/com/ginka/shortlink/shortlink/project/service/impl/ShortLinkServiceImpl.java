package com.ginka.shortlink.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ginka.shortlink.shortlink.project.common.constant.RedisKeyConstant;
import com.ginka.shortlink.shortlink.project.common.convention.exception.ClientException;
import com.ginka.shortlink.shortlink.project.common.convention.exception.ServiceException;
import com.ginka.shortlink.shortlink.project.common.enums.VailDateTypeEnum;
import com.ginka.shortlink.shortlink.project.dao.entity.ShortLinkDO;
import com.ginka.shortlink.shortlink.project.dao.entity.ShortLinkGotoDO;
import com.ginka.shortlink.shortlink.project.dao.mapper.LinkMapper;
import com.ginka.shortlink.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.ginka.shortlink.shortlink.project.dto.resp.ShortLinkCountQueryRespDTO;
import com.ginka.shortlink.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.ginka.shortlink.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.ginka.shortlink.shortlink.project.service.ShortLinkService;
import com.ginka.shortlink.shortlink.project.toolkit.HashUtil;
import com.ginka.shortlink.shortlink.project.toolkit.LinkUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import jodd.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<LinkMapper, ShortLinkDO> implements ShortLinkService {
    private final RBloomFilter<String> rBloomFilterConfiguration;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        //生成后缀
        String shortLinkSuffix = generateSuffix(requestParam);
        ShortLinkDO shortLinkDO = BeanUtil.toBean(requestParam, ShortLinkDO.class);
        shortLinkDO.setShortUri(shortLinkSuffix);
        shortLinkDO.setEnableStatus(0);
        // 拼接完整短链接
        shortLinkDO.setFullShortUrl(requestParam.getDomain()+"/"+shortLinkSuffix);
        ShortLinkGotoDO build = ShortLinkGotoDO.builder()
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .gid(requestParam.getGid())
                .build();
        shortLinkGotoMapper.insert(build);
        //布隆过滤器后防止漏判
        try {
            baseMapper.insert(shortLinkDO);
        }catch (DuplicateKeyException e){
            //todo 对误判的短链接怎么处理 1.存在于缓存 2.不存在于缓存 存在则抛异常 不存在则将其添加到布隆过滤器之中
            LambdaQueryWrapper<ShortLinkDO> eq = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, shortLinkDO.getFullShortUrl());
            ShortLinkDO shortLinkDO1 = baseMapper.selectOne(eq);
            if(shortLinkDO1!=null) {
                log.warn("短链接: {} 重复入库", shortLinkDO.getFullShortUrl());
                throw new ServiceException("短链接已存在");
            }
        }
        //缓存预热
        stringRedisTemplate.opsForValue().set(shortLinkDO.getFullShortUrl(),requestParam.getOriginUrl(), LinkUtil.getLinkCacheValidTime(requestParam.getValidDate()), TimeUnit.DAYS);
        rBloomFilterConfiguration.add(shortLinkDO.getFullShortUrl());
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl("https://"+shortLinkDO.getFullShortUrl())
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
        //完善返回的域名 convert 相当于forEach 对每一个元素进行转换
        return convert1.convert(item -> {
            ShortLinkPageRespDTO bean = BeanUtil.toBean(item, ShortLinkPageRespDTO.class);
            bean.setDomain("https://"+bean.getDomain());
            return bean;
        });
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
        baseMapper.delete(eq);
        baseMapper.insert(build1);

    }

    @SneakyThrows
    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) {
        String fullShortUrl = request.getServerName() + "/" + shortUri;
        //缓存击穿 在一个key失效后有大量的请求查询这个key
        String originLink = stringRedisTemplate.opsForValue().get(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl));
        if(StringUtil.isNotBlank(originLink)) {
            ((HttpServletResponse) response).sendRedirect(originLink);
            return;
        }
        boolean contains = rBloomFilterConfiguration.contains(fullShortUrl);
        if(!contains){
            return;
        }
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        if (StringUtil.isNotBlank(gotoIsNullShortLink)){
            return;
        }
        RLock lock = redissonClient.getLock(String.format(RedisKeyConstant.LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            //双重判定锁
            originLink = stringRedisTemplate.opsForValue().get(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl));
            if(StringUtil.isNotBlank(originLink)){
                ((HttpServletResponse)response).sendRedirect(originLink);
                return;
            }
            //用户传进来短链接 没有短链接gid信息 无法匹配分表键 用路由表来解决这一问题
            //具体实现为创建 一个新表 存shorturi 与 gid 拿到对应的gid 在通过gid查询原网址
            LambdaQueryWrapper<ShortLinkGotoDO> shortLinkGotoDOLambdaQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(shortLinkGotoDOLambdaQueryWrapper);
            if (shortLinkGotoDO == null) {
                stringRedisTemplate.opsForValue().set(String.format(RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-",30L, TimeUnit.MINUTES);
                return;
            }
            LambdaQueryWrapper<ShortLinkDO> eq = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .eq(ShortLinkDO::getDelFlag, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(eq);
            if (shortLinkDO != null) {
                stringRedisTemplate.opsForValue().set(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl), shortLinkDO.getOriginUrl());
                ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());
            }
        }finally {
            lock.unlock();
        }


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
