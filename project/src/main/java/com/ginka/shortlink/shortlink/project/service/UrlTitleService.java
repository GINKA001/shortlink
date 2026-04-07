package com.ginka.shortlink.shortlink.project.service;


import java.io.IOException;

public interface UrlTitleService {
    /**
     * 根据url获取短链接的标题
     * @param url 链接路径
     * @return 标题
     */
    String getTitleByUrl(String url ) throws IOException;
}
