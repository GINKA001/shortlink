package com.ginka.shortlink.shortlink.admin.remote.dto.req;

import lombok.Data;

/**
 * 短链接分页请求参数 根据分组进行分页查询
 */
@Data
public class ShortLinkPageReqDTO  {
    /**
     * 分组id
     */
    private String gid;

    private String current;

    private String size;
}
