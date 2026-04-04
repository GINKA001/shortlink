package com.ginka.shortlink.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ginka.shortlink.shortlink.admin.dao.entity.GroupDO;

public interface GroupService extends IService<GroupDO> {
    /**
     * 保存分组
     * @param groupName 分组名称
     */
    void saveGroup(String groupName);
}
