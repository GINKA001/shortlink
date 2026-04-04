package com.ginka.shortlink.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ginka.shortlink.shortlink.admin.common.database.BaseDO;
import lombok.Data;

import java.util.Date;

/**
 * 与数据库交互的实体类
 */
@Data
@TableName("t_user")
public class UserDO extends BaseDO {
    private Long id;
    private String username;
    private String password;
    private String real_name;
    private String phone;
    private String mail;
    private int deletion_time;

}
