package com.ginka.shortlink.shortlink.admin.controller;

import com.ginka.shortlink.shortlink.admin.common.convention.result.Result;
import com.ginka.shortlink.shortlink.admin.common.convention.result.Results;
import com.ginka.shortlink.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import com.ginka.shortlink.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.ginka.shortlink.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.ginka.shortlink.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.ginka.shortlink.shortlink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;
    @PostMapping("/api/short-link/admin/v1/group")
    public Result<Void> saveGroup(@RequestBody ShortLinkGroupSaveReqDTO requestParam) {
        groupService.saveGroup(requestParam.getGroupName());
        return Results.success();
    }

    /**
     * 短链接分组
     * @return
     */
    @GetMapping("/api/short-link/admin/v1/group")
    public Result<List<ShortLinkGroupRespDTO>> listGroup() {
        return Results.success(groupService.listGroup());
    }

    /**
     * 修改短链接分组 修改用putMapping
     * @return
     */
    @PutMapping("/api/short-link/admin/v1/group")
    public Result<Void> updateGroup(@RequestBody ShortLinkGroupUpdateReqDTO requestParam) {
        groupService.updateGroup(requestParam);
        return Results.success();
    }

    /**
     * 删除短链接分组
     * @return
     */
    @DeleteMapping("/api/short-link/admin/v1/group")
    public Result<Void> deleteGroup(@RequestParam String gid) {
        groupService.deleteGroup(gid);
        return Results.success();
    }
    /**
     * 短链接分组排序
     */
    @PostMapping("/api/short-link/admin/v1/group/sort")
    public Result<Void> sortGroup(@RequestBody List<ShortLinkGroupSortReqDTO> requestParam) {
        groupService.sortGroup(requestParam);
        return Results.success();
    }


}
