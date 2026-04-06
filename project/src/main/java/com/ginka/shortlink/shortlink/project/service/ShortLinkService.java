package com.ginka.shortlink.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ginka.shortlink.shortlink.project.dao.entity.ShortLinkDO;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.ginka.shortlink.shortlink.project.dto.resp.ShortLinkCountQueryRespDTO;
import com.ginka.shortlink.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.ginka.shortlink.shortlink.project.dto.resp.ShortLinkPageRespDTO;

import java.util.List;

public interface ShortLinkService extends IService<ShortLinkDO> {
    /**
     * 创建短链接
     * @param requestParam 创建链接请求参数
     * @return 创建链接响应参数
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam);

    /**
     * 分页查询短链接
     * @param requestParam 分页参数
     * @return 分页查询结果
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam);

    /**
     * 短链接数量查询
     * @return 短链接数量
     */
    List<ShortLinkCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam);
}
