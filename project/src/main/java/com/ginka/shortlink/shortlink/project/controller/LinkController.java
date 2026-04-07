package com.ginka.shortlink.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ginka.shortlink.shortlink.project.common.convention.result.Result;
import com.ginka.shortlink.shortlink.project.common.convention.result.Results;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.ginka.shortlink.shortlink.project.dto.resp.ShortLinkCountQueryRespDTO;
import com.ginka.shortlink.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.ginka.shortlink.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.ginka.shortlink.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接控制器
 */
@RestController
@RequiredArgsConstructor
public class LinkController {
    private final ShortLinkService shortLinkService;

    @PostMapping("/api/short-link/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return Results.success(shortLinkService.createShortLink(requestParam));
    }

    /**
     * 修改短链接
     * @param requestParam
     * @return
     */
    @PostMapping("/api/short-link/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        shortLinkService.updateShortLink(requestParam);
        return Results.success();
    }

    /**
     * 分页查询短链接
     * @param requestParam 分页参数0
     * @return  分页查询结果
     */
    @GetMapping("/api/short-link/v1/list")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return Results.success(shortLinkService.pageShortLink(requestParam));
    }

    /**
     *  短链接分组数量查询
     * @return
     */
    @GetMapping("/api/short-link/v1/count")
    public Result<List<ShortLinkCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam("requestParam") List<String> requestParam) {
        return Results.success(shortLinkService.listGroupShortLinkCount(requestParam));
    }
}
