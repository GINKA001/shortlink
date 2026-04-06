package com.ginka.shortlink.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ginka.shortlink.shortlink.project.dao.entity.ShortLinkDO;
import com.ginka.shortlink.shortlink.project.dto.resp.ShortLinkCountQueryRespDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LinkMapper extends BaseMapper<ShortLinkDO> {
    List<ShortLinkCountQueryRespDTO> listGroupShortLinkCount(@Param("requestParam") List<String> requestParam);
}
