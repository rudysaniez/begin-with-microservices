package com.me.microservices.core.composite.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.me.api.Actuator;
import com.me.api.Api;
import com.me.api.core.product.async.ProductAsyncService;
import com.me.api.core.product.health.ProductHealth;
import com.me.api.event.Event;
import com.me.microservices.core.composite.handler.exception.HandleHttpClientException;
import com.me.microservices.core.composite.producer.MessageProcessor;
import com.me.microservices.core.product.api.ProductsApi;
import com.me.microservices.core.product.api.model.PagedProduct;
import com.me.microservices.core.product.api.model.Product;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ProductIntegration implements ProductsApi, ProductAsyncService, ProductHealth {

	private final WebClient productClient;
	private final MessageProcessor messageProcessor;
	private final HandleHttpClientException handleException;
	
	@Autowired
	public ProductIntegration(WebClient.Builder webClientBuilder, MessageProcessor messageProcessor,
			HandleHttpClientException handleException,
			@Value("${app.product-service.host}") String productServiceHost,
			@Value("${app.product-service.port}") int productServicePort,
			@Value("${spring.webflux.base-path}") String basePath) {
		
		this.messageProcessor = messageProcessor;
		this.handleException = handleException;
		
		String productServiceUrl = new StringBuilder("http://").
				append(productServiceHost).append(":").append(productServicePort).
				append(basePath).toString();
		
		log.debug(" > Product service : " + productServiceUrl);
		
		this.productClient = webClientBuilder.uriBuilderFactory(new DefaultUriBuilderFactory(productServiceUrl)).build();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<Product>> getProduct(Integer productID, ServerWebExchange exchange) {
		
		return productClient.get().uri(uri -> uri.pathSegment(Api.PRODUCT_PATH, productID.toString()).build()).
				accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(Product.class).log().
				onErrorMap(WebClientResponseException.class, e -> handleException.handleHttpClientException(e)).
				map(p -> ResponseEntity.ok(p));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<PagedProduct>> findByName(String name, Integer pageNumber, Integer pageSize, ServerWebExchange exchange) {
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>(3);
		params.add("name", name);
		
		if(pageNumber != null && pageSize != null) {
			
			params.add("pageNumber", String.valueOf(pageNumber));
			params.add("pageSize", String.valueOf(pageSize));
		}
		
		return productClient.get().uri(uri -> uri.pathSegment(Api.PRODUCT_PATH).queryParams(params).build()).
				accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(PagedProduct.class).log().
				onErrorMap(WebClientResponseException.class, e -> handleException.handleHttpClientException(e)).
				map(pp -> ResponseEntity.ok(pp));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<Product>> save(Mono<Product> product, ServerWebExchange exchange) {
		
		return productClient.post().uri(uri -> uri.pathSegment(Api.PRODUCT_PATH).build()).
				body(product, Product.class).accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(Product.class).log().
				onErrorMap(WebClientResponseException.class, e -> handleException.handleHttpClientException(e)).
				map(p -> ResponseEntity.ok(p));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<Product>> update(Integer productID, Mono<Product> product, ServerWebExchange exchange) {
		
		return productClient.put().uri(uri -> uri.pathSegment(Api.PRODUCT_PATH, String.valueOf(productID)).build()).
				body(product, Product.class).accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(Product.class).log().
				onErrorMap(WebClientResponseException.class, e -> handleException.handleHttpClientException(e)).
				map(p -> ResponseEntity.ok(p));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<Void>> deleteProduct(Integer productID, ServerWebExchange exchange) {
		
		return productClient.delete().uri(uri -> uri.pathSegment(Api.PRODUCT_PATH, String.valueOf(productID)).build()).
				accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Void.class).log().
				onErrorMap(WebClientResponseException.class, e -> handleException.handleHttpClientException(e)).
				map(v -> ResponseEntity.ok(v));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteProductAsync(Integer productID) {
		
		Event<Integer> event = new Event<>(productID, Event.Type.DELETE);
		log.info(" > A product delete event will be sent : {}", event);
		messageProcessor.outputProducts().send(MessageBuilder.withPayload(event).build());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Health> getProductHealth() {
		
		return productClient.get().uri(uri -> uri.pathSegment(Actuator.BASE_PATH, Actuator.HEALTH_PATH).build()).
				retrieve().bodyToMono(String.class).map(s -> new Health.Builder().up().build()).
				onErrorResume(e -> Mono.just(new Health.Builder().down().build())).log();
	}
}
