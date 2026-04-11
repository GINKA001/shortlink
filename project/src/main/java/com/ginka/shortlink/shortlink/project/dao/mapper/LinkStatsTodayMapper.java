package com.ginka.shortlink.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ginka.shortlink.shortlink.project.dao.entity.LinkStatsTodayDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface LinkStatsTodayMapper extends BaseMapper<LinkStatsTodayDO> {
    @Insert("insert into t_link_stats_today(full_short_url,gid,date,today_pv,today_uv,today_uip,create_time,update_time,del_flag)" +
            " values(#{linkTodayStats.fullShortUrl},#{linkTodayStats.gid},#{linkTodayStats.date},#{linkTodayStats.todayPv},#{linkTodayStats.todayUv},#{linkTodayStats.todayUip},now(),now(),0)"+
            "ON DUPLICATE KEY UPDATE today_uv=today_uv+#{linkTodayStats.todayUv},today_pv=today_pv+#{linkTodayStats.todayPv},today_uip=today_uip+#{linkTodayStats.todayUip}" )
    void shortLinkTodayStats(@Param("linkTodayStats") LinkStatsTodayDO linkTodayStatsDO);
}
