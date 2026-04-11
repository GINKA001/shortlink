package com.ginka.shortlink.shortlink.admin.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ginka.shortlink.shortlink.admin.common.convention.result.Result;
import com.ginka.shortlink.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.ginka.shortlink.shortlink.admin.remote.dto.req.ShortLinkGroupStatsReqDTO;
import com.ginka.shortlink.shortlink.admin.remote.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.ginka.shortlink.shortlink.admin.remote.dto.req.ShortLinkStatsReqDTO;
import com.ginka.shortlink.shortlink.admin.remote.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.ginka.shortlink.shortlink.admin.remote.dto.resp.ShortLinkStatsRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接监控控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    ShortLinkRemoteService shortLinkRemoteService= new ShortLinkRemoteService(){};

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(ShortLinkStatsReqDTO requestParam) {
        return shortLinkRemoteService.oneShortLinkStats(requestParam);
    }
    @GetMapping("/api/short-link/v1/stats/group")
    public Result<ShortLinkStatsRespDTO> groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam) {
        return shortLinkRemoteService.groupShortLinkStats(requestParam);
    }
    /**
     *访问单个短链接规定时间内访问记录
     * @param requestParam 访问记录参数
     * @return 访问记录
     */
    @GetMapping("/api/short-link/admin/v1/stats/access-record")
    public Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        return shortLinkRemoteService.shortLinkStatsAccessRecord(requestParam);
    }
}