package com.fix.ticket_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = {"com.fix.common_service", "com.fix.ticket_service"})
public class TicketServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketServiceApplication.class, args);
	}

}
