package com.minipay.minipay_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableJpaAuditing
public class MinipayServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MinipayServiceApplication.class, args);
		System.out.println("MINI-PAY SERVICE STARTED SUCCESSFULLY!");
	}
}
