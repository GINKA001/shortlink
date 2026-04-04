package com.ginka.shortlink.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ginka.shortlink.shortlink.admin.dao.entity.GroupDO;
import com.ginka.shortlink.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;

import java.util.List;

public interface GroupService extends IService<GroupDO> {
    /**
     * 保存分组
     * @param groupName 分组名称
     */
    void saveGroup(String groupName);

    /**
     * 获取分组集合
     * @return 分组信息
     */
    List<ShortLinkGroupRespDTO> listGroup();
}
