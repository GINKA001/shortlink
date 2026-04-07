package com.ginka.shortlink.shortlink.project.controller;

import com.ginka.shortlink.shortlink.project.common.convention.result.Result;
import com.ginka.shortlink.shortlink.project.common.convention.result.Results;
import com.ginka.shortlink.shortlink.project.service.UrlTitleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class UrlTitleController {
    private final UrlTitleService urlTitleService;
    /**
     * 根据url获取短链接的标题
     * @param url 链接路径
     * @return 标题
     */
    @GetMapping("/api/short-link/v1/title")
    public Result<String> getTitleByUrl(@RequestParam("url") String url ) throws IOException {
        return Results.success(urlTitleService.getTitleByUrl( url));
    }
}
