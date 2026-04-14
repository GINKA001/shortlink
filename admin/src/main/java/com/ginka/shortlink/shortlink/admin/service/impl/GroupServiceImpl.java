package com.ginka.shortlink.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ginka.shortlink.shortlink.admin.common.biz.user.UserContext;
import com.ginka.shortlink.shortlink.admin.common.convention.exception.ClientException;
import com.ginka.shortlink.shortlink.admin.common.convention.result.Result;
import com.ginka.shortlink.shortlink.admin.dao.entity.GroupDO;
import com.ginka.shortlink.shortlink.admin.dao.mapper.GroupMapper;
import com.ginka.shortlink.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.ginka.shortlink.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.ginka.shortlink.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.ginka.shortlink.shortlink.admin.remote.ShortLinkRemoteService;
import com.ginka.shortlink.shortlink.admin.remote.dto.resp.ShortLinkCountQueryRespDTO;
import com.ginka.shortlink.shortlink.admin.service.GroupService;
import com.ginka.shortlink.shortlink.admin.util.RandomCodeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.ginka.shortlink.shortlink.admin.common.constant.RedisCacheConstant.LOCK_GROUP_CREATE_KEY;

/**
 * 短链接分组服务实现层
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {
    ShortLinkRemoteService shortLinkRemoteService=new ShortLinkRemoteService(){};
    private  final RedissonClient redissonClient;

    @Value("${short-link.group.max-num}")
    private Integer maxGroupSize;

    @Override
    public void saveGroup(String groupName){
        saveGroup(UserContext.getUsername(),groupName);
    }
    @Override
    public void saveGroup(String username,String groupName) {
        RLock lock = redissonClient.getLock(String.format(LOCK_GROUP_CREATE_KEY, username));
        lock.lock();
        try {
            String gid="";
            LambdaQueryWrapper<GroupDO> eq1 = Wrappers.lambdaQuery(GroupDO.class).eq(GroupDO::getUsername, username)
                    .eq(GroupDO::getDelFlag, 0);
            List<GroupDO> groupDOS = baseMapper.selectList(eq1);
            if(CollUtil.isNotEmpty(groupDOS) && groupDOS.size() == maxGroupSize){
                throw new ClientException(String.format("超出最大分组数:%d",maxGroupSize));
            }
            while(true) {
                gid = RandomCodeUtils.generateSecureRandomCode();
                LambdaQueryWrapper<GroupDO> eq = Wrappers.lambdaQuery(GroupDO.class)
                        .eq(GroupDO::getGid, gid)
                        // 设置用户名
                        .eq(GroupDO::getUsername,username);
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
        }finally {
            lock.unlock();
        }

    }

    @Override
    public List<ShortLinkGroupRespDTO> listGroup() {
        String username = UserContext.getUsername();
        // 添加用户名
        LambdaQueryWrapper<GroupDO> groupDOLambdaQueryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0)
                .orderByDesc(GroupDO::getSortOrder, GroupDO::getUpdateTime);
        List<GroupDO> groupDOS = baseMapper.selectList(groupDOLambdaQueryWrapper);
        Result<List<ShortLinkCountQueryRespDTO>> listResult = shortLinkRemoteService.listGroupShortLinkCount(groupDOS.stream().map(GroupDO::getGid).toList());
        List<ShortLinkGroupRespDTO> shortLinkGroupRespDTOS = BeanUtil.copyToList(groupDOS, ShortLinkGroupRespDTO.class);
        shortLinkGroupRespDTOS.forEach(each -> {
            Optional<ShortLinkCountQueryRespDTO> first = listResult.getData().stream().filter(item-> Objects.equals(item.getGid(), each.getGid())).findFirst();
            first.ifPresent(item->each.setShortLinkCount(first.get().getShortLinkCount()));
        });
        return shortLinkGroupRespDTOS;
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

    @Override
    public void deleteGroup(String gid) {
//        LambdaQueryWrapper<GroupDO> eq = Wrappers.lambdaQuery(GroupDO.class).eq(GroupDO::getGid, gid);
//        GroupDO groupDO = baseMapper.selectOne(eq);
//        if(groupDO==null){
//            throw new ClientException("分组不存在");
//        }
//        baseMapper.delete(eq);
        //软删除
        LambdaQueryWrapper<GroupDO> eq = Wrappers.lambdaQuery(GroupDO.class).eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getGid,gid)
                .eq(GroupDO::getDelFlag, 0);
        GroupDO groupDO = new GroupDO();
        groupDO.setDelFlag(1);
        baseMapper.update(groupDO, eq);
    }
/**
 * 短链接分组排序
 * 更新排序字段
 */
    @Override
    public void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam) {
        requestParam.forEach(item -> {
            GroupDO groupDO=GroupDO.builder()
                    .username(UserContext.getUsername())
                    .sortOrder(item.getSortOrder())
                    .build();
            LambdaQueryWrapper<GroupDO> eq = Wrappers.lambdaQuery(GroupDO.class)
                    .eq(GroupDO::getUsername, UserContext.getUsername())
                    .eq(GroupDO::getGid, item.getGid())
                    .eq(GroupDO::getDelFlag, 0);
            baseMapper.update(groupDO, eq);
        });
    }
}
