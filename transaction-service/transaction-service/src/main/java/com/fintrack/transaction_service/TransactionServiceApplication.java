package com.fintrack.transaction_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient  // For Eureka registration
@EnableFeignClients     // For Feign clients


public class TransactionServiceApplication {

	public static void main(String[] args) {

		SpringApplication.run(TransactionServiceApplication.class, args);
		System.out.println(" Transaction Service running on port 8083");

	}

}
