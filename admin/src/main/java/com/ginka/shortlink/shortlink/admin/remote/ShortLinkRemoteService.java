package com.ginka.shortlink.shortlink.admin.remote;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ginka.shortlink.shortlink.admin.common.convention.result.Result;
import com.ginka.shortlink.shortlink.admin.remote.dto.req.*;
import com.ginka.shortlink.shortlink.admin.remote.dto.resp.*;

import java.util.HashMap;
import java.util.List;
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
        param.put("orderTag",requestParam.getOrderTag());
        param.put("size", requestParam.getSize());
        String s = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/list", param);//请求路径 127.0.0.1:8001/api/v1/short-link/page 传入参数 gid,current,size
        return JSON.parseObject(s, new TypeReference<Result<IPage<ShortLinkPageRespDTO>>>(){});
    }
    default Result<List<ShortLinkCountQueryRespDTO>> listGroupShortLinkCount(List<String> requestParam) {
        Map<String, Object> param = new HashMap<>();
        param.put("requestParam", requestParam);
        String s = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/count", param);
        return JSON.parseObject(s, new TypeReference<Result<List<ShortLinkCountQueryRespDTO>>>(){});
    }
    default void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        String s = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/update",JSON.toJSONString(requestParam));
    }

    default Result<String> getTitleByUrl(String url ) {
        String s = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/title?url="+url);
        return  JSON.parseObject(s, new TypeReference<Result<String>>(){});
    }
    default void saveRecycleBin(RecycleBinSaveReqDTO requestParam) {
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/save",JSON.toJSONString(requestParam));
    }
    default Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        Map<String, Object> param = new HashMap<>();
        param.put("gidList", requestParam.getGidList());
        param.put("current", requestParam.getCurrent());
        param.put("size", requestParam.getSize());
        String s = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/recycle_bin/list", param);//请求路径 127.0.0.1:8001/api/v1/short-link/page 传入参数 gid,current,size
        return JSON.parseObject(s, new TypeReference<Result<IPage<ShortLinkPageRespDTO>>>(){});
    }
    default Result<Void> recoverRecycleBin(RecycleBinRecoverReqDTO requestParam){
        String s = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/recover",JSON.toJSONString(requestParam));
        return JSON.parseObject(s, new TypeReference<Result<Void>>(){});
    }

    default void removeRecycleBin(RecycleBinDeleteReqDTO requestParam){
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/remove",JSON.toJSONString(requestParam));
    }
    default Result<ShortLinkStatsRespDTO> oneShortLinkStats(ShortLinkStatsReqDTO requestParam) {
        String resultBodyStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats", BeanUtil.beanToMap(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>(){});}
    default Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(requestParam);
        stringObjectMap.remove("orders");
        stringObjectMap.remove("records");
        String s = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats/access-record",stringObjectMap);
        return JSON.parseObject(s, new TypeReference<>(){});
    }

    default Result<ShortLinkStatsRespDTO> groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam) {
        String s = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats/group", BeanUtil.beanToMap(requestParam));
        return JSON.parseObject(s, new TypeReference<>() {});
    }
    default Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkGroupStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam) {
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(requestParam);
        stringObjectMap.remove("orders");
        stringObjectMap.remove("records");
        String s = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats/access-record/group", stringObjectMap);
        return JSON.parseObject(s, new TypeReference<>() {});
    }

    default Result<ShortLinkBatchCreateRespDTO> batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam) {
        String post = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/create/batch", BeanUtil.beanToMap(requestParam));
        return JSON.parseObject(post, new TypeReference<>() {});
    }
}
