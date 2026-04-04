package com.ginka.shortlink.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ginka.shortlink.shortlink.admin.dao.entity.GroupDO;
import com.ginka.shortlink.shortlink.admin.dao.mapper.GroupMapper;
import com.ginka.shortlink.shortlink.admin.service.GroupService;
import com.ginka.shortlink.shortlink.admin.util.RandomCodeUtils;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 短链接分组服务实现层
 */
@Service
@Slf4j
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    @Override
    public void saveGroup(String groupName) {
        String gid="";
        while(true) {
            gid = RandomCodeUtils.generateSecureRandomCode();
            LambdaQueryWrapper<GroupDO> eq = Wrappers.lambdaQuery(GroupDO.class).eq(GroupDO::getGid, gid)
                    //todo 设置用户名
                    .eq(GroupDO::getUsername, null);
            if (baseMapper.selectOne(eq) == null) {
                break;
            }
        }
        GroupDO build = GroupDO.builder().gid(gid)
                .name(groupName)
                .build();
        baseMapper.insert(build);
    }
}
