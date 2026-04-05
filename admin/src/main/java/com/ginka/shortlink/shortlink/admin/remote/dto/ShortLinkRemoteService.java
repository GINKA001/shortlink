package com.ginka.shortlink.shortlink.admin.remote.dto;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ginka.shortlink.shortlink.admin.common.convention.exception.RemoteException;
import com.ginka.shortlink.shortlink.admin.common.convention.result.Result;
import com.ginka.shortlink.shortlink.admin.common.convention.result.Results;
import com.ginka.shortlink.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.ginka.shortlink.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.ginka.shortlink.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.ginka.shortlink.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ginka
 * 短链接中台调用服务
 * @date 2023/9/27
 */
public interface ShortLinkRemoteService {
    default Result<ShortLinkCreateRespDTO> createShortLink(ShortLinkCreateReqDTO requestParam) {
        Map<String, Object> param = new HashMap<>();
        param.put("domain", requestParam.getDomain());
        param.put("originUrl", requestParam.getOriginUrl());
        param.put("gid", requestParam.getGid());
        param.put("createdType", requestParam.getCreatedType());
        param.put("validDateType", requestParam.getValidDateType());
        param.put("validDate", requestParam.getValidDate());
        param.put("describe", requestParam.getDescribe());
        // ✅ 发送真正的 POST 请求，参数在 Body 中以 JSON 格式传输
        String s = HttpUtil.createPost("http://127.0.0.1:8001/api/short-link/v1/create")
                .body(JSON.toJSONString(requestParam))  // 将对象序列化为 JSON
                .contentType("application/json")         // 设置 Content-Type
                .execute()
                .body();

        return JSON.parseObject(s, new TypeReference<Result<ShortLinkCreateRespDTO>>(){});
    }
    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        Map<String, Object> param = new HashMap<>();
        param.put("gid", requestParam.getGid());
        param.put("current", requestParam.getCurrent());
        param.put("size", requestParam.getSize());
        String s = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/list", param);//请求路径 127.0.0.1:8001/api/v1/short-link/page 传入参数 gid,current,size
        return JSON.parseObject(s, new TypeReference<Result<IPage<ShortLinkPageRespDTO>>>(){});
    }
}
