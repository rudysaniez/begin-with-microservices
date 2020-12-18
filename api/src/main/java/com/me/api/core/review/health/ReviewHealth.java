package com.me.api.core.review.health;

import org.springframework.boot.actuate.health.Health;

import reactor.core.publisher.Mono;

public interface ReviewHealth {

	public Mono<Health> getReviewHealth();
}
