package com.ginka.shortlink.shortlink.project.controller;

import com.ginka.shortlink.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接控制器
 */
@RestController
@RequiredArgsConstructor
public class LinkController {
    private final ShortLinkService shortLinkService;

}
