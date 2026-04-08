package com.ginka.shortlink.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ginka.shortlink.shortlink.project.common.convention.result.Result;
import com.ginka.shortlink.shortlink.project.common.convention.result.Results;
import com.ginka.shortlink.shortlink.project.dto.req.RecycleBinRecoverReqDTO;
import com.ginka.shortlink.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.ginka.shortlink.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.ginka.shortlink.shortlink.project.service.RecycleBinService;
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
    @PostMapping("/api/short-link/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam){
        recycleBinService.saveRecycleBin(requestParam);
        return Results.success();
    }
    /**
     * 分页查询短链接
     * @param requestParam 分页参数
     * @return  分页查询结果
     */
    @GetMapping("/api/short-link/v1/recycle-bin/list")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        return Results.success(recycleBinService.pageShortLink(requestParam));
    }
    @PostMapping("/api/short-link/v1/recycle-bin/recover")
    public Result<Void> recoverRecycleBin(@RequestBody RecycleBinRecoverReqDTO requestParam){
        recycleBinService.recoverRecycleBin(requestParam);
        return Results.success();
    }

}
