package com.ginka.shortlink.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_user")
public class UserDO {
    private Long id;
    private String username;
    private String password;
    private String real_name;
    private String phone;
    private String mail;
    private int deletion_time;
    private Date create_time;
    private Date update_time;
    private int del_flag;


}
