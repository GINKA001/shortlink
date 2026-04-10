package com.ginka.shortlink.shortlink.project.dto.req;

import lombok.Data;

import java.util.Date;
@Data
public class ShortLinkStatusChongGouReqDTO {
    private String gid;
    private String fullShortUrl;
    private Date startDate;
    private Date endDate;
}
