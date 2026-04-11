package com.ginka.shortlink.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ginka.shortlink.shortlink.project.dao.entity.LinkAccessStatsDO;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 访问统计Mapper
 * 短链接基础访问持久层
 * @author ginka //gid不用管功能正常实现 编译器问题
 */
@Mapper
public interface LinkAccessStatsMapper extends BaseMapper<LinkAccessStatsDO> {
    @Insert("insert into t_link_access_stats(full_short_url,gid,date,pv,uv,uip,hour,weekday,create_time,update_time,del_flag)" +
            " values(#{linkAccessStats.fullShortUrl},#{linkAccessStats.gid},#{linkAccessStats.date},#{linkAccessStats.pv},#{linkAccessStats.uv},#{linkAccessStats.uip}, #{linkAccessStats.hour},#{linkAccessStats.weekday},now(),now(),0)"+
            "ON DUPLICATE KEY UPDATE pv=pv+#{linkAccessStats.pv},uv=uv+#{linkAccessStats.uv},uip=uip+#{linkAccessStats.uip}" )
    void shortLinkStats(@Param("linkAccessStats")LinkAccessStatsDO linkAccessStatsdo);
    /**
     * 根据短链接获取指定日期内基础监控数据
     */
    @Select("SELECT " +
            "    date, " +
            "    SUM(pv) AS pv, " +
            "    SUM(uv) AS uv, " +
            "    SUM(uip) AS uip " +
            "FROM " +
            "    t_link_access_stats " +
            "WHERE " +
            "    full_short_url = #{param.fullShortUrl} " +
            "    AND gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    full_short_url, gid, date;")
    List<LinkAccessStatsDO> listStatsByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

    /**
     * 根据短链接获取指定日期内小时基础监控数据
     */
    @Select("SELECT " +
            "    hour, " +
            "    SUM(pv) AS pv " +
            "FROM " +
            "    t_link_access_stats " +
            "WHERE " +
            "    full_short_url = #{param.fullShortUrl} " +
            "    AND gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    full_short_url, gid, hour;")
    List<LinkAccessStatsDO> listHourStatsByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

    /**
     * 根据短链接获取指定日期内小时基础监控数据
     */
    @Select("SELECT " +
            "    weekday, " +
            "    SUM(pv) AS pv " +
            "FROM " +
            "    t_link_access_stats " +
            "WHERE " +
            "    full_short_url = #{param.fullShortUrl} " +
            "    AND gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    full_short_url, gid, weekday;")
    List<LinkAccessStatsDO> listWeekdayStatsByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

    @Select("select date ,sum(pv) AS pv,sum(uv) as uv,sum(uip) as uip from t_link_access_stats where gid=#{param.gid}    and date between #{param.startDate} and #{param.endDate} group by gid,date;")
    List<LinkAccessStatsDO> listStatsByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);
    @Select("SELECT " +
            "    hour, " +
            "    SUM(pv) AS pv " +
            "FROM " +
            "    t_link_access_stats " +
            "WHERE " +
            "     gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "     gid, hour;")
    List<LinkAccessStatsDO> listHourStatsByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);

    @Select("SELECT " +
            "    weekday, " +
            "    SUM(pv) AS pv " +
            "FROM " +
            "    t_link_access_stats " +
            "WHERE " +
            "     gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "     gid, weekday;")
    List<LinkAccessStatsDO> listWeekdayStatsByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);
}
