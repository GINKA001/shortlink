package com.ginka.shortlink.shortlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ginka.shortlink.shortlink.project.dao.entity.ShortLinkDO;
import com.ginka.shortlink.shortlink.project.dto.req.RecycleBinSaveReqDTO;

/**
 * 回收站服务接口
 */
public interface RecycleBinService extends IService<ShortLinkDO> {
    void saveRecycleBin(RecycleBinSaveReqDTO reqDTO);
}
