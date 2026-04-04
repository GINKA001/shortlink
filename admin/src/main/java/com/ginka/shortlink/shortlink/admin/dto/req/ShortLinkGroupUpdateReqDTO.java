package com.ginka.shortlink.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 短链接分组修改参数
 */
@Data
public class ShortLinkGroupUpdateReqDTO {
/**
 * 短链接分组更新参数
 */
    private String gid;
    private String name;
}
