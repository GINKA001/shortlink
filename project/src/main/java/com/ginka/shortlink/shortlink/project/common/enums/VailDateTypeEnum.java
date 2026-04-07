package com.ginka.shortlink.shortlink.project.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 有效期类型枚举
 */
@RequiredArgsConstructor
public enum VailDateTypeEnum {
    PERMANENT(0),
    CUSTOM(1);
    @Getter
    private final int type;

}
