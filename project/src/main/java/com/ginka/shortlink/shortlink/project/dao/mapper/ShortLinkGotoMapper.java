package com.ginka.shortlink.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ginka.shortlink.shortlink.project.dao.entity.ShortLinkGotoDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 短链接跳转持久层
 *
 * @author ginka
 * @date 2023/07/09
 */
@Mapper
public interface ShortLinkGotoMapper extends BaseMapper<ShortLinkGotoDO> {
}
