package com.ginka.shortlink.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ginka.shortlink.shortlink.admin.common.biz.user.UserContext;
import com.ginka.shortlink.shortlink.admin.dao.entity.GroupDO;
import com.ginka.shortlink.shortlink.admin.dao.mapper.GroupMapper;
import com.ginka.shortlink.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.ginka.shortlink.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.ginka.shortlink.shortlink.admin.service.GroupService;
import com.ginka.shortlink.shortlink.admin.util.RandomCodeUtils;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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
            LambdaQueryWrapper<GroupDO> eq = Wrappers.lambdaQuery(GroupDO.class)
                    .eq(GroupDO::getGid, gid)
                    // 设置用户名
                    .eq(GroupDO::getUsername, UserContext.getUsername());
            if (baseMapper.selectOne(eq) == null) {
                break;
            }
        }
        GroupDO build = GroupDO.builder()
                .username(UserContext.getUsername())
                .gid(gid)
                .name(groupName)
                .build();
        baseMapper.insert(build);
    }

    @Override
    public List<ShortLinkGroupRespDTO> listGroup() {
        // 添加用户名
        LambdaQueryWrapper<GroupDO> groupDOLambdaQueryWrapper = Wrappers.lambdaQuery(GroupDO.class).eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0)
                .orderByDesc(GroupDO::getSortOrder, GroupDO::getUpdateTime);
        List<GroupDO> groupDOS = baseMapper.selectList(groupDOLambdaQueryWrapper);
        return BeanUtil.copyToList(groupDOS, ShortLinkGroupRespDTO.class);//copyToList 批量转换
    }

    @Override
    public void updateGroup(ShortLinkGroupUpdateReqDTO requestParam) {
        LambdaQueryWrapper<GroupDO> eq = Wrappers.lambdaQuery(GroupDO.class).eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getGid, requestParam.getGid())
                .eq(GroupDO::getDelFlag, 0);
        GroupDO groupDO = new GroupDO();
        groupDO.setName(requestParam.getName());
        baseMapper.update(groupDO, eq);
    }
}
