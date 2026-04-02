package com.ginka.shortlink.shortlink.admin.dto.resp;

import lombok.Data;

@Data
public class UserActualRespDTO {
    private Long id;
    private String username;
    private String real_name;
    private String phone;
    private String mail;
}
