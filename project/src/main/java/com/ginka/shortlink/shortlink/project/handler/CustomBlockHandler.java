package com.ginka.shortlink.shortlink.project.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.ginka.shortlink.shortlink.project.common.convention.result.Result;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ginka.shortlink.shortlink.project.dto.resp.ShortLinkCreateRespDTO;

/**
 * 自定义限流处理逻辑
 */
public class CustomBlockHandler {
    public static Result<ShortLinkCreateRespDTO> createShortLinkBlockHandlerMethod(ShortLinkCreateReqDTO requestParam, BlockException exception) {
        return new Result<ShortLinkCreateRespDTO>().setCode("B100000").setMessage("当前访问网站人数过多，请稍后再试...");
    }
}