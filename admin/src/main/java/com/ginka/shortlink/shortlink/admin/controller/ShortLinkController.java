package com.ginka.shortlink.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ginka.shortlink.shortlink.admin.common.convention.result.Result;
import com.ginka.shortlink.shortlink.admin.common.convention.result.Results;
import com.ginka.shortlink.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.ginka.shortlink.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.ginka.shortlink.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.ginka.shortlink.shortlink.admin.remote.dto.resp.ShortLinkCountQueryRespDTO;
import com.ginka.shortlink.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.ginka.shortlink.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

public class ShortLinkController {
    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService(){};
        return shortLinkRemoteService.createShortLink(requestParam);
    }
    @GetMapping("/api/short-link/admin/v1/list")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService(){};
        return shortLinkRemoteService.pageShortLink(requestParam);
    }
    @GetMapping("/api/short-link/admin/v1/count")
    public Result<List<ShortLinkCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam("requestParam") List<String> requestParam) {
        ShortLinkRemoteService shortLinkService = new ShortLinkRemoteService(){};
        return shortLinkService.listGroupShortLinkCount(requestParam);
    }
}
