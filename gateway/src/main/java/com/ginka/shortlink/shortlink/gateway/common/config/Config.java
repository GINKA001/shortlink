package com.ginka.shortlink.shortlink.gateway.common.config;

import lombok.Data;
/**
 * 过滤器配置类
 */
import java.util.List;
@Data
public class Config {
    /**
     * 白名单前置路径
     */
    private List<String> whitePathList;
}
