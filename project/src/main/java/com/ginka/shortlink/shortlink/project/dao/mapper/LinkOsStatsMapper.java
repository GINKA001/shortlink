package com.ginka.shortlink.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ginka.shortlink.shortlink.project.dao.entity.LinkOsStatsDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 操作系统访问统计持久层
 */
@Mapper
public interface LinkOsStatsMapper extends BaseMapper<LinkOsStatsDO> {
    @Insert("insert into t_link_os_stats(full_short_url,gid,date,cnt,os,create_time,update_time,del_flag)" +
            " values(#{linkOsStats.fullShortUrl},#{linkOsStats.gid},#{linkOsStats.date},#{linkOsStats.cnt},#{linkOsStats.os},now(),now(),0)"+
            "ON DUPLICATE KEY UPDATE cnt=cnt+#{linkAccessStats.cnt}" )
    void shortLinkOsStats(@Param("linkOsStats") LinkOsStatsDO linkOsStatsDO);
}
