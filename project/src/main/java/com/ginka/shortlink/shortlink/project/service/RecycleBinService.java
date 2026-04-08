package com.ginka.shortlink.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ginka.shortlink.shortlink.project.dao.entity.ShortLinkDO;
import com.ginka.shortlink.shortlink.project.dto.req.RecycleBinDeleteReqDTO;
import com.ginka.shortlink.shortlink.project.dto.req.RecycleBinRecoverReqDTO;
import com.ginka.shortlink.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.ginka.shortlink.shortlink.project.dto.resp.ShortLinkPageRespDTO;

/**
 * 回收站服务接口
 */
public interface RecycleBinService extends IService<ShortLinkDO> {
    /**
     * 保存短链接到回收站
     * @param requestParam 保存参数
     */
    void saveRecycleBin(RecycleBinSaveReqDTO requestParam);
    /**
     * 分页查询短链接
     * @param requestParam 分页参数
     * @return 分页查询结果
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam);

    /**
     * 恢复短链接短链
     * @param requestParam 恢复参数
     */
    void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam);
    /**
     * 删除回收站内的短链接
     * @param requestParam 删除参数
     */
    void removeRecycleBin(RecycleBinDeleteReqDTO requestParam);
}
