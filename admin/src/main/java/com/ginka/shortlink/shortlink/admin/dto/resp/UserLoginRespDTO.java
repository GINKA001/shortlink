package com.ginka.shortlink.shortlink.admin.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 用户登录接口返回响应
 * @Author: Ginka
 * @Date: 2022/1/27 10:05
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginRespDTO {
    String token;
}
