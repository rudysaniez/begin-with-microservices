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
import com.me.api.core.review.async.ReviewAsyncService;
import com.me.api.core.review.health.ReviewHealth;
import com.me.api.event.Event;
import com.me.microservices.core.composite.handler.exception.HandleHttpClientException;
import com.me.microservices.core.composite.producer.MessageProcessor;
import com.me.microservices.core.review.api.ReviewsApi;
import com.me.microservices.core.review.api.model.PagedReview;
import com.me.microservices.core.review.api.model.Review;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ReviewIntegration implements ReviewsApi, ReviewAsyncService, ReviewHealth {

	private final WebClient reviewClient;
	private final MessageProcessor messageProcessor;
	private final HandleHttpClientException handleException;
	
	@Autowired
	public ReviewIntegration(WebClient.Builder webClientBuilder, MessageProcessor messageProcessor, 
			HandleHttpClientException handleException,
			@Value("${app.review-service.host}") String reviewServiceHost,
			@Value("${app.review-service.port}") int reviewServicePort,
			@Value("${spring.webflux.base-path}") String basePath) {
		
		this.messageProcessor = messageProcessor;
		this.handleException = handleException;
		
		String reviewServiceUrl =new StringBuilder("http://").
				append(reviewServiceHost).append(":").append(reviewServicePort).
				append(basePath).toString();
		
		log.debug(" > Review service : " + reviewServiceUrl);
		
		this.reviewClient = webClientBuilder.uriBuilderFactory(new DefaultUriBuilderFactory(reviewServiceUrl)).build();
	}
	
	/**
	 * {@inheritDocy}
	 */
	@Override
	public Mono<ResponseEntity<Review>> getReview(Integer reviewID, ServerWebExchange exchange) {
		
		return reviewClient.get().uri(uriFunction -> uriFunction.pathSegment(Api.REVIEW_PATH, String.valueOf(reviewID)).build()).
				accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(Review.class).log().
				onErrorMap(WebClientResponseException.class, e -> handleException.handleHttpClientException(e)).
				map(r -> ResponseEntity.ok(r));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<PagedReview>> getReviewByProductId(Integer productID, Integer pageNumber, Integer pageSize, ServerWebExchange exchange) {
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>(3);
		params.add("productId", String.valueOf(productID));
		
		if(pageNumber != null && pageSize != null) {
			
			params.add("pageNumber", String.valueOf(pageNumber));
			params.add("pageSize", String.valueOf(pageSize));
		}
		
		return reviewClient.get().uri(uri -> uri.pathSegment(Api.REVIEW_PATH).queryParams(params).build()).
				accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(PagedReview.class).
					onErrorResume(e -> Mono.empty()).
				map(pr -> ResponseEntity.ok(pr)).
				log();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<Review>> save(Mono<Review> review, ServerWebExchange exchange) {
		
		return reviewClient.post().uri(uri -> uri.pathSegment(Api.REVIEW_PATH).build()).
				body(review, Review.class).accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(Review.class).
					onErrorMap(WebClientResponseException.class, e -> handleException.handleHttpClientException(e)).
				map(r -> ResponseEntity.ok(r)).
				log();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<Review>> update(Integer reviewID, Mono<Review> review, ServerWebExchange exchange) {
		
		return reviewClient.put().uri(uri -> uri.pathSegment(Api.REVIEW_PATH, String.valueOf(reviewID)).build()).
				body(review, Review.class).accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(Review.class).
					onErrorMap(WebClientResponseException.class, e -> handleException.handleHttpClientException(e)).
				map(r -> ResponseEntity.ok(r)).
				log();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<Void>> deleteReviews(Integer reviewID, ServerWebExchange exchange) {
		
		return reviewClient.delete().uri(uri -> uri.pathSegment(Api.REVIEW_PATH, String.valueOf(reviewID)).build()).
				accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(Void.class).log().
					onErrorMap(WebClientResponseException.class, e -> handleException.handleHttpClientException(e)).
				map(v -> ResponseEntity.ok(v)).
				log();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteReviewsAsync(Integer productID) {
		
		Event<Integer> event = new Event<>(productID, Event.Type.DELETE);
		log.info(" > A review delete event will be sent : {}", event);
		messageProcessor.outputReviews().send(MessageBuilder.withPayload(event).build());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Health> getReviewHealth() {
		
		return reviewClient.get().uri(uri -> uri.pathSegment(Actuator.BASE_PATH, Actuator.HEALTH_PATH).build()).
				retrieve().bodyToMono(String.class).map(s -> new Health.Builder().up().build()).
				onErrorResume(e -> Mono.just(new Health.Builder().down().build()));
	}
}
