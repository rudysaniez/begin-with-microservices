package com.me.microservices.core.composite;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.me.api.core.product.health.ProductHealth;
import com.me.api.core.recommendation.health.RecommendationHealth;
import com.me.api.core.review.health.ReviewHealth;
import com.me.microservices.core.composite.producer.MessageProcessor;

import lombok.Getter;
import lombok.Setter;
import reactor.core.publisher.Mono;

@EnableBinding(value = MessageProcessor.class)
@EnableConfigurationProperties(value=Application.PaginationInformation.class)
@ComponentScan(basePackages= {"com.me.microservices.core", "com.me.handler.http"})
@SpringBootApplication
public class Application {
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Getter @Setter
	@ConfigurationProperties(prefix="api.pagination")
	public static class PaginationInformation {
		
		private int pageNumber;
		private int pageSize;
	}
	
	
	@Autowired private ProductHealth productHealth;
	@Autowired private RecommendationHealth recommendationHealth;
	@Autowired private ReviewHealth reviewHealth;
	
	@Bean
	public CompositeReactiveHealthContributor coreMicroservices() {
		
		ReactiveHealthContributor productContributor = new ReactiveHealthIndicator() {
			@Override
			public Mono<Health> health() {
				return productHealth.getProductHealth();
			}
		};
		
		ReactiveHealthContributor recommendationContributor = new ReactiveHealthIndicator() {
			@Override
			public Mono<Health> health() {
				return recommendationHealth.getRecommendationHealth();
			}
		};
		
		ReactiveHealthContributor reviewContributor = new ReactiveHealthIndicator() {
			@Override
			public Mono<Health> health() {
				return reviewHealth.getReviewHealth();
			}
		};
		
		Map<String, ReactiveHealthContributor> contributors = new HashMap<>();
		contributors.put("product", productContributor);
		contributors.put("recommendation", recommendationContributor);
		contributors.put("review", reviewContributor);
		
		return CompositeReactiveHealthContributor.fromMap(contributors);
	}
}
