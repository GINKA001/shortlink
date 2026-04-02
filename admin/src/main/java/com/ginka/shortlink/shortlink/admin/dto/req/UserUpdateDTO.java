package com.ginka.shortlink.shortlink.admin.dto.req;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String username;
    private String password;
    private String real_name;
    private String phone;
    private String mail;
}
