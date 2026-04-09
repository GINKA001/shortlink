package com.ginka.shortlink.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@TableName("t_link_access_stats")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LinkAccessStatsDO {
    /**
     * 短链接id
     */
    private Long id;
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 统计日期
     */
    private Date date;

    /**
     * 访问量（PV）
     */
    private Integer pv;

    /**
     * 独立访客数（UV）
     */
    private Integer uv;

    /**
     * 独立IP数
     */
    private Integer uip;

    /**
     * 小时维度
     */
    private Integer hour;

    /**
     * 星期维度
     */
    private Integer weekday;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 删除标识：0-未删除，1-已删除（逻辑删除）
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;
}
