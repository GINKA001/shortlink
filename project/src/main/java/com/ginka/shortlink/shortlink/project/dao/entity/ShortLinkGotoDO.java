package com.ginka.shortlink.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接跳转实体
 * @author ginka
 */
@TableName("t_link_goto")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShortLinkGotoDO {
    private Long id;
    private String gid;
    private String fullShortUrl;
}
