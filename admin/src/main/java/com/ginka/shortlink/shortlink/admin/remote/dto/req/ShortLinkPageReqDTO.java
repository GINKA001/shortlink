package com.ginka.shortlink.shortlink.admin.remote.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * 短链接分页请求参数 根据分组进行分页查询
 */
@Data
public class ShortLinkPageReqDTO extends Page {
    /**
     * 分组id
     */
    private String gid;
    /**
     * 排序字段
     */
    private String orderTag;
}
