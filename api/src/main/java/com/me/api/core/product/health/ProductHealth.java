package com.me.api.core.product.health;

import org.springframework.boot.actuate.health.Health;

import reactor.core.publisher.Mono;

public interface ProductHealth {

	public Mono<Health> getProductHealth();
}
