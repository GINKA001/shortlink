package com.ginka.shortlink.shortlink.project;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ginka.shortlink.shortlink.project.dao.mapper")
public class ShortLinkApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(ShortLinkApplication.class, args);
    }
}
