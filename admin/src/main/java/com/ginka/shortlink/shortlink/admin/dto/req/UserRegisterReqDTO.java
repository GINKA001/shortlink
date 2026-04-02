package com.ginka.shortlink.shortlink.admin.dto.req;
import lombok.Data;

import java.util.Date;

/**
 * 用户注册请求参数
 */
@Data
public class UserRegisterReqDTO {
    private String username;
    private String password;
    private String real_name;
    private String phone;
    private String mail;

}
