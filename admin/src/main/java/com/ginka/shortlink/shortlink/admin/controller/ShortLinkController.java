package com.ginka.shortlink.shortlink.admin.controller;



import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ginka.shortlink.shortlink.admin.common.convention.result.Result;
import com.ginka.shortlink.shortlink.admin.common.convention.result.Results;
import com.ginka.shortlink.shortlink.admin.remote.ShortLinkActualRemoteService;
import com.ginka.shortlink.shortlink.admin.remote.dto.req.ShortLinkBatchCreateReqDTO;
import com.ginka.shortlink.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.ginka.shortlink.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.ginka.shortlink.shortlink.admin.remote.dto.req.ShortLinkUpdateReqDTO;
import com.ginka.shortlink.shortlink.admin.remote.dto.resp.*;
import com.ginka.shortlink.shortlink.admin.util.EasyExcelWebUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShortLinkController {
    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return shortLinkActualRemoteService.createShortLink(requestParam);
    }

    /**
     * 批量创建短链接
     */
    @SneakyThrows
    @PostMapping("/api/short-link/admin/v1/create/batch")
    public void batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParam, HttpServletResponse response) {
        Result<ShortLinkBatchCreateRespDTO> shortLinkBatchCreateRespDTOResult = shortLinkActualRemoteService.batchCreateShortLink(requestParam);
        if (shortLinkBatchCreateRespDTOResult.isSuccess()) {
            List<ShortLinkBaseInfoRespDTO> baseLinkInfos = shortLinkBatchCreateRespDTOResult.getData().getBaseLinkInfos();
            EasyExcelWebUtil.write(response, "批量创建短链接-SaaS短链接系统", ShortLinkBaseInfoRespDTO.class, baseLinkInfos);
        }
    }
    @GetMapping("/api/short-link/admin/v1/list")
    public Result<Page<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return shortLinkActualRemoteService.pageShortLink(requestParam.getGid(),requestParam.getOrderTag(),requestParam.getCurrent(),requestParam.getSize());
    }
    /**
     * 修改短链接
     * @param requestParam 修改参数
     * @return  修改结果
     */
    @PostMapping("/api/short-link/admin/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        shortLinkActualRemoteService.updateShortLink(requestParam);
        return Results.success();
    }
    @GetMapping("/api/short-link/admin/v1/count")
    public Result<List<ShortLinkCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam("requestParam") List<String> requestParam) {
        return shortLinkActualRemoteService.listGroupShortLinkCount(requestParam);
    }
}
