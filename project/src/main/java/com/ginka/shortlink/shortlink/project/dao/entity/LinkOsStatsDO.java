package com.ginka.shortlink.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ginka.shortlink.shortlink.project.common.database.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 操作系统访问统计实体类
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
@TableName("t_link_os_stats")
@Builder
public class LinkOsStatsDO extends BaseDO {

    /**
     * ID
     */
    private Long id;

    /**
     * 唯一标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 日期
     */
    private Date date;

    /**
     * 访问量
     */
    private Integer cnt;

    /**
     * 操作系统名称
     */
    private String os;
}
