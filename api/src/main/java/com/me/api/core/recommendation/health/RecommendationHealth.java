package com.me.api.core.recommendation.health;

import org.springframework.boot.actuate.health.Health;

import reactor.core.publisher.Mono;

public interface RecommendationHealth {

	public Mono<Health> getRecommendationHealth();
}
