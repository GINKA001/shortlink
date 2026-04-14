package com.ginka.shortlink.shortlink.admin.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ginka.shortlink.shortlink.admin.common.convention.result.Result;
import com.ginka.shortlink.shortlink.admin.common.convention.result.Results;
import com.ginka.shortlink.shortlink.admin.remote.ShortLinkRemoteService;
import com.ginka.shortlink.shortlink.admin.remote.dto.req.*;
import com.ginka.shortlink.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.ginka.shortlink.shortlink.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回收站控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {
    private final RecycleBinService recycleBinService;
    ShortLinkRemoteService shortLinkRemoteService=new ShortLinkRemoteService(){};
    @PostMapping("/api/short-link/admin/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam){
        shortLinkRemoteService.saveRecycleBin(requestParam);
        return Results.success();
    }
    @GetMapping("/api/short-link/admin/v1/recycle-bin/list")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        return recycleBinService.pageRecycleBinShortLink(requestParam);
    }
/**
 * 恢复短链接
 */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/recover")
    public Result<Void> recoverRecycleBin(@RequestBody RecycleBinRecoverReqDTO requestParam){
        return shortLinkRemoteService.recoverRecycleBin(requestParam);
    }

    @PostMapping("/api/short-link/admin/v1/recycle-bin/remove")
    public Result<Void> removeRecycleBin(@RequestBody RecycleBinDeleteReqDTO requestParam){
        shortLinkRemoteService.removeRecycleBin(requestParam);
        return Results.success();
    }
}
