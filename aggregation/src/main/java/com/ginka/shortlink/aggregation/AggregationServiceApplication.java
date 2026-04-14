package com.ginka.shortlink.aggregation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@MapperScan(value = {
        "com.ginka.shortlink.shortlink.project.dao.mapper",
        "com.ginka.shortlink.shortlink.admin.dao.mapper"})
@SpringBootApplication(scanBasePackages = {"com.ginka.shortlink.aggregation", "com.ginka.shortlink.shortlink.project", "com.ginka.shortlink.shortlink.admin"})
public class AggregationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AggregationServiceApplication.class, args);
    }

}
