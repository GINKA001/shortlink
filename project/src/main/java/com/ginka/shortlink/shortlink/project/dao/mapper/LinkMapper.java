package com.ginka.shortlink.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ginka.shortlink.shortlink.project.dao.entity.ShortLinkDO;
import com.ginka.shortlink.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.ginka.shortlink.shortlink.project.dto.resp.ShortLinkCountQueryRespDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface LinkMapper extends BaseMapper<ShortLinkDO> {
    List<ShortLinkCountQueryRespDTO> listGroupShortLinkCount(@Param("requestParam") List<String> requestParam);
    @Update("update t_link set total_pv=total_pv+#{totalPv},total_uv=total_uv+#{totalUv},total_uip=total_uip+#{totalUip} where gid=#{gid} and full_short_url=#{fullShortUrl} ")
    void incrementStats(@Param("gid") String gid, @Param("fullShortUrl") String fullShortUrl, @Param("totalPv") Integer totalPv, @Param("totalUv") Integer totalUv, @Param("totalUip") Integer totalUip);


    Page<ShortLinkDO> pageLink(ShortLinkPageReqDTO requestParam);
}
