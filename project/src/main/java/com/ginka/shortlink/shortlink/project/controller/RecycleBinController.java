package com.ginka.shortlink.shortlink.project.controller;

import com.ginka.shortlink.shortlink.project.common.convention.result.Result;
import com.ginka.shortlink.shortlink.project.common.convention.result.Results;
import com.ginka.shortlink.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.ginka.shortlink.shortlink.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
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

}
