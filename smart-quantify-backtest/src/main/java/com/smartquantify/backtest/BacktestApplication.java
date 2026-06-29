package com.smartquantify.backtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class BacktestApplication {
    public static void main(String[] args) {
        SpringApplication.run(BacktestApplication.class, args);
    }
}