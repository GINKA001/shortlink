package com.ginka.shortlink.shortlink.admin.dto.resp;

import lombok.Data;
/**
 * 短链接分组响应参数
 */
@Data
public class ShortLinkGroupRespDTO {

    private String gid;
    private String name;
    private Integer sortOrder;
    /**
     * 分组下短链接数量
     */
    private Integer shortLinkCount;
}
