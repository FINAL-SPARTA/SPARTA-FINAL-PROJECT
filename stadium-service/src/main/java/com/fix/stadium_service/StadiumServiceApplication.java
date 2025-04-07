package com.fix.stadium_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {"com.fix.common_service", "com.fix.stadium_service"})
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableFeignClients
public class StadiumServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(StadiumServiceApplication.class, args);
	}

}
