package com.ginka.shortlink.shortlink.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 短链接风控配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "short-link.user-flow")
public class UserFlowRiskControlConfiguration {
    /**
     * 是否开启用户流量风控
     */
    private Boolean enable;

    /**
     * 用户流量风控时间窗口
     */
    private String timeWindow;

    /**
     * 用户流量风控阈值
     */
    private Long maxAccessCount;
}
