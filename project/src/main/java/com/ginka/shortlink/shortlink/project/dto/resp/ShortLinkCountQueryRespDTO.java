package com.ginka.shortlink.shortlink.project.dto.resp;

import lombok.Data;

/**
 * 短链接分组查询返回
 */
@Data
public class ShortLinkCountQueryRespDTO {
    private String gid;
    private Integer ShortLinkCount;
}
