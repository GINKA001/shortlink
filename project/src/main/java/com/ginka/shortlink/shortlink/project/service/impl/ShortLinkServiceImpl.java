package com.ginka.shortlink.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ginka.shortlink.shortlink.project.dao.entity.ShortLinkDO;
import com.ginka.shortlink.shortlink.project.dao.mapper.LinkMapper;
import com.ginka.shortlink.shortlink.project.service.ShortLinkService;
import org.springframework.stereotype.Service;

@Service
public class ShortLinkServiceImpl extends ServiceImpl<LinkMapper, ShortLinkDO> implements ShortLinkService {
}
