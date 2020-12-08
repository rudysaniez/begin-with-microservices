package com.me.api.core.health;

import org.springframework.boot.actuate.health.Health;

import reactor.core.publisher.Mono;

public interface HealthService {

	public Mono<Health> getProductHealth();
	
	public Mono<Health> getRecommendationHealth();
	
	public Mono<Health> getReviewHealth();
}
