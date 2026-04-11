package com.ginka.shortlink.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ginka.shortlink.shortlink.project.dao.entity.LinkAccessLogsDO;
import com.ginka.shortlink.shortlink.project.dao.entity.LinkAccessStatsDO;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkStatusChongGouReqDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 访问日志监控持久层
 */
@Mapper
public interface LinkAccessLogsMapper extends BaseMapper<LinkAccessLogsDO> {
    /**
     * 根据短链接获取指定日期内高频访问IP数据
     */
    @Select("SELECT " +
            "    ip, " +
            "    COUNT(ip) AS count " +
            "FROM " +
            "    t_link_access_logs " +
            "WHERE " +
            "    full_short_url = #{param.fullShortUrl} " +
            "    AND gid = #{param.gid} " +
            "    AND create_time BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    full_short_url, gid, ip " +
            "ORDER BY " +
            "    count DESC " +
            "LIMIT 5;")
    List<HashMap<String, Object>> listTopIpByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

    /**
     * 根据短链接获取指定日期内新旧访客数据
     */
    @Select("SELECT " +
            "    SUM(old_user) AS oldUserCnt, " +
            "    SUM(new_user) AS newUserCnt " +
            "FROM ( " +
            "    SELECT " +
            "        CASE WHEN COUNT(DISTINCT DATE(create_time)) > 1 THEN 1 ELSE 0 END AS old_user, " +
            "        CASE WHEN COUNT(DISTINCT DATE(create_time)) = 1 AND MAX(create_time) >= #{param.startDate} AND MAX(create_time) <= #{param.endDate} THEN 1 ELSE 0 END AS new_user " +
            "    FROM " +
            "        t_link_access_logs " +
            "    WHERE " +
            "        full_short_url = #{param.fullShortUrl} " +
            "        AND gid = #{param.gid} " +
            "    GROUP BY " +
            "        user " +
            ") AS user_counts;")
    HashMap<String, Object> findUvTypeCntByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);


    List<Map<String, Object>> selectUvTypeByUsers(@Param("param") ShortLinkStatusChongGouReqDTO requestParam, @Param("userAccessLogsList")List<String> list);

    /**
     * 根据短链接获取指定日期内的pv uv uip
     * @param requestParam 请求参数
     * @return 访问数据
     */
    @Select(" SELECT full_short_url, gid, COUNT(user) AS pv, COUNT(DISTINCT user) AS uv, COUNT(DISTINCT ip) AS uip FROM t_link_access_logs WHERE full_short_url = #{param.fullShortUrl} AND gid = #{param.gid} AND create_time BETWEEN #{param.startDate} AND #{param.endDate} GROUP BY full_short_url, gid;"
    )
    LinkAccessStatsDO findPvUvUipStatusByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);
    @Select(" SELECT full_short_url, gid, COUNT(user) AS pv, COUNT(DISTINCT user) AS uv, COUNT(DISTINCT ip) AS uip FROM t_link_access_logs WHERE gid = #{param.gid} AND create_time BETWEEN #{param.startDate} AND #{param.endDate} GROUP BY gid;"
    )
    LinkAccessStatsDO findPvUvUipStatusByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);

    @Select("SELECT " +
            "    ip, " +
            "    COUNT(ip) AS count " +
            "FROM " +
            "    t_link_access_logs " +
            "WHERE " +
            "     gid = #{param.gid} " +
            "    AND create_time BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    full_short_url, gid, ip " +
            "ORDER BY " +
            "    count DESC " +
            "LIMIT 5;")
    List<HashMap<String, Object>> listTopIpByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);

    @Select("SELECT " +
            "    SUM(old_user) AS oldUserCnt, " +
            "    SUM(new_user) AS newUserCnt " +
            "FROM ( " +
            "    SELECT " +
            "        CASE WHEN COUNT(DISTINCT DATE(create_time)) > 1 THEN 1 ELSE 0 END AS old_user, " +
            "        CASE WHEN COUNT(DISTINCT DATE(create_time)) = 1 AND MAX(create_time) >= #{param.startDate} AND MAX(create_time) <= #{param.endDate} THEN 1 ELSE 0 END AS new_user " +
            "    FROM " +
            "        t_link_access_logs " +
            "    WHERE " +
            "        gid = #{param.gid} " +
            "    GROUP BY " +
            "        user " +
            ") AS user_counts;")
    HashMap<String, Object> findUvTypeCntByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);
}
