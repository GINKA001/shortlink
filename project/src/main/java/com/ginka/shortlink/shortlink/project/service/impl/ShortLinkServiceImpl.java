package com.ginka.shortlink.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ginka.shortlink.shortlink.project.common.constant.RedisKeyConstant;
import com.ginka.shortlink.shortlink.project.common.constant.ShortLinkConstant;
import com.ginka.shortlink.shortlink.project.common.convention.exception.ClientException;
import com.ginka.shortlink.shortlink.project.common.convention.exception.ServiceException;
import com.ginka.shortlink.shortlink.project.common.enums.VailDateTypeEnum;
import com.ginka.shortlink.shortlink.project.dao.entity.*;
import com.ginka.shortlink.shortlink.project.dao.mapper.*;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.ginka.shortlink.shortlink.project.dto.resp.*;
import com.ginka.shortlink.shortlink.project.service.ShortLinkService;
import com.ginka.shortlink.shortlink.project.toolkit.HashUtil;
import com.ginka.shortlink.shortlink.project.toolkit.LinkUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jodd.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<LinkMapper, ShortLinkDO> implements ShortLinkService {
    private final RBloomFilter<String> rBloomFilterConfiguration;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;
    private final LinkStatsTodayMapper linkStatsTodayMapper;

    @Value("${short-link.stats.local.amap-key}")
    private String statsLocalamapKey;
    @Value("${short-link.domain.default}")
    private String domainDefault;
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) throws IOException {
        //生成后缀
        String shortLinkSuffix = generateSuffix(requestParam);
        ShortLinkDO shortLinkDO = BeanUtil.toBean(requestParam, ShortLinkDO.class);
        shortLinkDO.setShortUri(shortLinkSuffix);
        shortLinkDO.setEnableStatus(0);
        shortLinkDO.setFavicon(getFavicon(requestParam.getOriginUrl()));
        shortLinkDO.setTotalPv(0);
        shortLinkDO.setTotalUv(0);
        shortLinkDO.setTotalUip(0);
        // 拼接完整短链接
        shortLinkDO.setFullShortUrl(domainDefault+"/"+shortLinkSuffix);
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
        stringRedisTemplate.opsForValue()
                .set(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, shortLinkDO.getFullShortUrl()),
                        requestParam.getOriginUrl(),
                        LinkUtil.getLinkCacheValidTime(requestParam.getValidDate()), TimeUnit.MILLISECONDS);
        rBloomFilterConfiguration.add(shortLinkDO.getFullShortUrl());
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl("https://"+shortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .build();
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {

        IPage<ShortLinkDO> page0 = baseMapper.pageLink(requestParam);
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
        String severPort = Optional.of(request.getServerPort()).filter(each -> !Objects.equals(each, 80)).map(String::valueOf).map(each -> ":" + each).orElse("");
        String fullShortUrl = request.getServerName() + severPort + "/" + shortUri;

        //缓存击穿 在一个key失效后有大量的请求查询这个key
        String originLink = stringRedisTemplate.opsForValue().get(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl));
        if(StringUtil.isNotBlank(originLink)) {
            shortLinkStats(fullShortUrl,null, request, response);
            ((HttpServletResponse) response).sendRedirect(originLink);
            return;
        }
        boolean contains = rBloomFilterConfiguration.contains(fullShortUrl);
        if(!contains){
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        if (StringUtil.isNotBlank(gotoIsNullShortLink)){
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
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
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            LambdaQueryWrapper<ShortLinkDO> eq = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .eq(ShortLinkDO::getDelFlag, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(eq);
            if (shortLinkDO == null || shortLinkDO.getValidDate().before(new Date())) {
                stringRedisTemplate.opsForValue().set(String.format(RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-",30L, TimeUnit.MINUTES);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            stringRedisTemplate.opsForValue().set(
                    String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl)
                    , shortLinkDO.getOriginUrl()
                    ,LinkUtil.getLinkCacheValidTime(shortLinkDO.getValidDate()),TimeUnit.MILLISECONDS
            );
            ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());
        }finally {
            lock.unlock();
        }
    }

    @SneakyThrows
    @Override
    public ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam) {
        List<String> describes = requestParam.getDescribes();
        List<String> originUrls = requestParam.getOriginUrls();
        List<ShortLinkBaseInfoRespDTO> response = new ArrayList<>();
        int size=originUrls.size();
        for (int i = 0; i < size; i++){
            ShortLinkCreateReqDTO shortLinkCreateReqDTO = BeanUtil.toBean(requestParam, ShortLinkCreateReqDTO.class);
            shortLinkCreateReqDTO.setOriginUrl(originUrls.get(i));
            shortLinkCreateReqDTO.setDescribe(describes.get(i));
            try {
                ShortLinkCreateRespDTO shortLink = createShortLink(shortLinkCreateReqDTO);
                ShortLinkBaseInfoRespDTO shortLinkBaseInfoRespDTO = ShortLinkBaseInfoRespDTO.builder().fullShortUrl(shortLink.getFullShortUrl())
                        .originUrl(shortLink.getOriginUrl())
                        .describe(describes.get(i))
                        .build();
                response.add(shortLinkBaseInfoRespDTO);
            }catch (Throwable e){
                log.error("创建短链接失败 {}",originUrls.get(i));
            }
        }
        return ShortLinkBatchCreateRespDTO.builder()
                .total(response.size())
                .baseLinkInfos( response)
                .build();
    }

    private void shortLinkStats(String fullShortUrl,String gid,ServletRequest request, ServletResponse response){
        //用cookie来判断使用的用户数
        AtomicBoolean isNew = new AtomicBoolean();
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        AtomicReference<String> uv= new AtomicReference<>(); //唯一标识
        Runnable addResponseCookie = ()->{
            uv.set(UUID.fastUUID().toString());
            Cookie cookie = new Cookie("pv", uv.get());
            cookie.setMaxAge(60 * 60 * 24 * 30);
            cookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
            ((HttpServletResponse)response).addCookie(cookie);
            isNew.set(Boolean.TRUE);
            stringRedisTemplate.opsForSet().add("short-link:status:uv:" , fullShortUrl + uv.get());
        };
        if (ArrayUtil.isNotEmpty( cookies)){
            Arrays.stream(cookies).filter(each-> Objects.equals(each.getName(), "uv")).findFirst().map(Cookie::getValue)
                    .ifPresentOrElse(item->
                    {
                        uv.set(item);
                        Long add = stringRedisTemplate.opsForSet().add("short-link:status:uv:" + fullShortUrl , item);
                        isNew.set(add!=null && add>0L);
                    }, addResponseCookie);
        }else {
            addResponseCookie.run();
        }
        String remoteAddr = LinkUtil.getActualIp((HttpServletRequest) request);
        Long uipAdd = stringRedisTemplate.opsForSet().add("short-link:status:uip:" + fullShortUrl , remoteAddr);
        boolean uipFirstFlag = uipAdd!=null && uipAdd>0L;
        if(StrUtil.isBlank(gid)){
            LambdaQueryWrapper<ShortLinkGotoDO> shortLinkGotoDOLambdaQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(shortLinkGotoDOLambdaQueryWrapper);
            gid = shortLinkGotoDO.getGid();
        }
        int hour = DateUtil.hour(new Date(), true);//获取小时
        Week week = DateUtil.dayOfWeekEnum(new Date());
        int value = week.getIso8601Value();//获取星期
        LinkAccessStatsDO build = LinkAccessStatsDO.builder()
                .pv(1)
                .hour(hour)
                .uv(isNew.get()?1:0)
                .uip(uipFirstFlag?1:0)
                .weekday(value)
                .fullShortUrl(fullShortUrl)
                .date(new Date())
                .gid(gid)
                .build();
        linkAccessStatsMapper.shortLinkStats(build);
        Map<String, Object> localParamMap = new HashMap<>();
        localParamMap.put("key",statsLocalamapKey);
        localParamMap.put("ip", remoteAddr);
        String s = HttpUtil.get(ShortLinkConstant.AMAP_REMOTE_URL, localParamMap);
        JSONObject jsonObject = JSON.parseObject(s);
        String infoCode = jsonObject.getString("infocode");
        String province;
        String city;
        LinkLocaleStatsDO linkLocaleStatsDO;
        if(StrUtil.isNotBlank(infoCode)&& infoCode.equals("10000")){
            province = jsonObject.getString("province");
            boolean unknownFlag=StrUtil.equals(province,"[]");
            linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .gid(gid)
                    .cnt(1)
                    .country("中国")
                    .province(province=unknownFlag?"未知" : province)
                    .city(city=unknownFlag?"未知":jsonObject.getString("city"))
                    .adCode(unknownFlag?"未知":jsonObject.getString("adcode"))
                    .build();
            linkLocaleStatsMapper.shortLinkLocaleState(linkLocaleStatsDO);
            String os = LinkUtil.getOs((HttpServletRequest)request);
            LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                    .gid( gid)
                    .fullShortUrl(fullShortUrl)
                    .cnt(1)
                    .os(os)
                    .date(new Date())
                    .build();
            linkOsStatsMapper.shortLinkOsStats(linkOsStatsDO);
            String  browser=LinkUtil.getBrowser(((HttpServletRequest) request));
            LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                    .browser(browser)
                    .cnt(1)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .build();
            linkBrowserStatsMapper.shortLinkBrowserState(linkBrowserStatsDO);

            String device = LinkUtil.getDevice(((HttpServletRequest) request));
            LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                    .device(device)
                    .cnt(1)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .build();
            linkDeviceStatsMapper.shortLinkDeviceState(linkDeviceStatsDO);
            String network = LinkUtil.getNetwork(((HttpServletRequest) request));
            LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                    .network(network)
                    .cnt(1)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .build();
            linkNetworkStatsMapper.shortLinkNetworkState(linkNetworkStatsDO);
            LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                    .ip(remoteAddr)
                    .browser(browser)
                    .network(network)
                    .device(device)
                    .os(os)
                    .locale("中国"+"-"+province+"-"+city)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .user(uv.get())
                    .build();
            linkAccessLogsMapper.insert(linkAccessLogsDO);

            baseMapper.incrementStats(gid,fullShortUrl,1,isNew.get()?1:0,uipFirstFlag?1:0);
            LinkStatsTodayDO statsTodayDO = LinkStatsTodayDO.builder().gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .todayPv(1)
                    .todayUv(isNew.get() ? 1 : 0)
                    .todayUip(uipFirstFlag ? 1 : 0)
                    .build();
            linkStatsTodayMapper.shortLinkTodayStats(statsTodayDO);
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
    private String getFavicon(String url) throws IOException {
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
            String redirectUrl = connection.getHeaderField("Location");
            if (redirectUrl != null) {
                URL newUrl = new URL(redirectUrl);
                connection = (HttpURLConnection) newUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                responseCode = connection.getResponseCode();
            }
        }
        if (responseCode == HttpURLConnection.HTTP_OK) {
            Document document = Jsoup.connect(url).get();
            Element faviconLink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (faviconLink != null) {
                return faviconLink.attr("abs:href");
            }
        }
        return null;
    }
}
