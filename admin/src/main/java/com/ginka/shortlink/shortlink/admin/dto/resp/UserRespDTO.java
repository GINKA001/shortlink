package com.ginka.shortlink.shortlink.admin.dto.resp;

import lombok.Data;

/**
 * 用户返回DTO
 */
@Data
public class UserRespDTO {
    private Long id;
    private String username;
    private String real_name;
    private String phone;
    private String mail;
}
