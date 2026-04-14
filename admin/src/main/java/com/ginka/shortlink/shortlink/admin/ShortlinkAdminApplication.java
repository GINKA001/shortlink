package com.ginka.shortlink.shortlink.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = "com.ginka.shortlink")
@SpringBootApplication
@EnableDiscoveryClient
@FeignClient("com/ginka/shortlink/shortlink/admin/remote")
public class ShortlinkAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShortlinkAdminApplication.class, args);
    }
}
