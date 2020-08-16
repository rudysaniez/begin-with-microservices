package com.me.work.example.microservices.core.composite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

@ComponentScan(basePackages= {"com.me.work.example.microservices.core", "com.me.work.example.handler.http"})
@SpringBootApplication
public class Application {
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	/**
	 * Not reactive client, but here it's for a simple example.
	 * @return {@link RestTemplate}
	 */
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
