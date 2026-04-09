package com.ginka.shortlink.shortlink.project.toolkit;



import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.ginka.shortlink.shortlink.project.common.constant.ShortLinkConstant;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.Optional;

/**
 * 链接工具类
 */
public class LinkUtil {
    /**
     * 获取链接缓存有效期
     * @param validDate 有效期时间
     * @return 有效期时间戳
     */
    public static long getLinkCacheValidTime(Date validDate) {
        if (validDate.before(new Date())){
            return 1;
        }
        return Optional.ofNullable(validDate).map(each-> DateUtil.between(new Date(),each, DateUnit.MS)).orElse(ShortLinkConstant.DEFAULT_CACHE_VALID_TIME);
    }
    /**
     * 获取请求的 IP 地址
     * @param request 请求
     * @return 用户ip地址
     */
    public static String getActualIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Http_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
