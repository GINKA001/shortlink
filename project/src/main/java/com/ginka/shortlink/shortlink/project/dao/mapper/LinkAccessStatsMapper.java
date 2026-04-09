package com.ginka.shortlink.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ginka.shortlink.shortlink.project.dao.entity.LinkAccessStatsDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 访问统计Mapper
 * 短链接基础访问持久层
 * @author ginka
 */
@Mapper
public interface LinkAccessStatsMapper extends BaseMapper<LinkAccessStatsDO> {
    @Insert("insert into t_link_access_stats(full_short_url,gid,date,pv,uv,uip,hour,weekday,create_time,update_time,del_flag)" +
            " values(#{linkAccessStats.fullShortUrl},#{linkAccessStats.gid},#{linkAccessStats.date},#{linkAccessStats.pv},#{linkAccessStats.uv},#{linkAccessStats.uip}, #{linkAccessStats.hour},#{linkAccessStats.weekday},now(),now(),0)"+
            "ON DUPLICATE KEY UPDATE pv=pv+#{linkAccessStats.pv},uv=uv+#{linkAccessStats.uv},uip=uip+#{linkAccessStats.uip}" )
    void shortLinkStats(@Param("linkAccessStats")LinkAccessStatsDO linkAccessStatsdo);
}
