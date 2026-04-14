package com.ginka.shortlink.shortlink.admin.controller;


import com.ginka.shortlink.shortlink.admin.common.convention.result.Result;
import com.ginka.shortlink.shortlink.admin.remote.ShortLinkActualRemoteService;
import com.ginka.shortlink.shortlink.admin.remote.ShortLinkRemoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UrlTitleController {
    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 根据url获取短链接的标题
     * @param url 链接路径
     * @return 标题
     */
    @GetMapping("/api/short-link/admin/v1/title")
    public Result<String> getTitleByUrl(@RequestParam("url") String url ) {
        return shortLinkActualRemoteService.getTitleByUrl(url);
    }
}
