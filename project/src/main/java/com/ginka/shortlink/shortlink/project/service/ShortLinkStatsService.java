package com.ginka.shortlink.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.ginka.shortlink.shortlink.project.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.ginka.shortlink.shortlink.project.dto.resp.ShortLinkStatsRespDTO;

/**
 * 短链接监控接口层
 */
public interface ShortLinkStatsService {

    /**
     * 获取单个短链接监控数据
     *
     * @param requestParam 获取短链接监控数据入参
     * @return 短链接监控数据
     */
    ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO requestParam);

    IPage<ShortLinkStatsAccessRecordRespDTO> oneShortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam);
    ShortLinkStatsRespDTO groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam);
}
