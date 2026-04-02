package com.ginka.shortlink.shortlink.admin.dto.resp;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ginka.shortlink.shortlink.admin.common.serialize.PhoneDesensitizationSerializer;
import lombok.Data;

/**
 * 用户返回DTO
 */
@Data
public class UserRespDTO {
    private Long id;
    private String username;
    private String real_name;
    @JsonSerialize(using = PhoneDesensitizationSerializer.class)//@JsonSerialize 添加此注解，指定序列化器 在返回给前端时，经过序列化器，将手机号进行脱敏处理
    private String phone;
    private String mail;
}
