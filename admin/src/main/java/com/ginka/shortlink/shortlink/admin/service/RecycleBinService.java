package com.ginka.shortlink.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ginka.shortlink.shortlink.admin.common.convention.result.Result;
import com.ginka.shortlink.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.ginka.shortlink.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;


public interface RecycleBinService {
    /**
     * 分页查询回收站短链接
     */
    Result<Page<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam);
}
