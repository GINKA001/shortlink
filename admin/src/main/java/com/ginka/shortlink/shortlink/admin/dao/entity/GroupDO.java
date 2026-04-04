package com.ginka.shortlink.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 短链接分组实体
 */
@Data
@TableName("t_group")
public class GroupDO {
    private Long id;
    private String gid;
    private String name;
    private String username;
    private Data createTime;
    private Data updateTime;
    private Integer delFlag;
}
