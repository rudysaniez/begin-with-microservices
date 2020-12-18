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
import com.me.api.core.recommendation.async.RecommendationAsyncService;
import com.me.api.core.recommendation.health.RecommendationHealth;
import com.me.api.event.Event;
import com.me.microservices.core.composite.handler.exception.HandleHttpClientException;
import com.me.microservices.core.composite.producer.MessageProcessor;
import com.me.microservices.core.recommendation.api.RecommendationsApi;
import com.me.microservices.core.recommendation.api.model.PagedRecommendation;
import com.me.microservices.core.recommendation.api.model.Recommendation;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class RecommendationIntegration implements RecommendationsApi, RecommendationAsyncService, RecommendationHealth {

	private final WebClient recommendationClient;
	private final MessageProcessor messageProcessor;
	private final HandleHttpClientException handleException;
	
	@Autowired
	public RecommendationIntegration(WebClient.Builder webClientBuilder, MessageProcessor messageProcessor, 
			HandleHttpClientException handleException,
			@Value("${app.recommendation-service.host}") String recommendationServiceHost,
			@Value("${app.recommendation-service.port}") int recommendationServicePort,
			@Value("${spring.webflux.base-path}") String basePath) {
		
		this.messageProcessor = messageProcessor;
		this.handleException = handleException;
		
		String recommendationServiceUrl = new StringBuilder("http://").
				append(recommendationServiceHost).append(":").append(recommendationServicePort).
				append(basePath).toString();
		
		log.debug(" > Recommendation service : " + recommendationServiceUrl);
		
		this.recommendationClient = webClientBuilder.uriBuilderFactory(new DefaultUriBuilderFactory(recommendationServiceUrl)).build();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<Recommendation>> getRecommendation(Integer recommendationID, ServerWebExchange exchange) {
		
		return recommendationClient.get().uri(uriFunction -> uriFunction.pathSegment(Api.RECOMMENDATION_PATH, String.valueOf(recommendationID)).build()).
				accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Recommendation.class).log().
				onErrorMap(WebClientResponseException.class, e -> handleException.handleHttpClientException(e)).
				map(r -> ResponseEntity.ok(r));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<PagedRecommendation>> getRecommendationByProductId(Integer productID, Integer pageNumber, Integer pageSize, ServerWebExchange exchange) {
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>(3);
		params.add("productId", String.valueOf(productID));
		
		if(pageNumber != null && pageSize != null) {
			
			params.add("pageNumber", String.valueOf(pageNumber));
			params.add("pageSize", String.valueOf(pageSize));
		}
		
		return recommendationClient.get().uri(uriFunction -> uriFunction.pathSegment(Api.RECOMMENDATION_PATH).queryParams(params).build()).
				accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(PagedRecommendation.class).log().
				onErrorResume(e -> Mono.empty()).
				map(r -> ResponseEntity.ok(r));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<Recommendation>> save(Mono<Recommendation> recommendation, ServerWebExchange exchange) {
		
		return recommendationClient.post().uri(uri -> uri.pathSegment(Api.RECOMMENDATION_PATH).build()).
				body(recommendation, Recommendation.class).accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(Recommendation.class).log().
				onErrorMap(WebClientResponseException.class, e -> handleException.handleHttpClientException(e)).
				map(r -> ResponseEntity.ok(r));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<Recommendation>> update(Integer recommendationID, Mono<Recommendation> recommendation, ServerWebExchange exchange) {
		
		return recommendationClient.put().uri(uriFunction -> uriFunction.pathSegment(Api.RECOMMENDATION_PATH, String.valueOf(recommendationID)).build()).
				body(recommendation, Recommendation.class).accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(Recommendation.class).log().
				onErrorMap(WebClientResponseException.class, e -> handleException.handleHttpClientException(e)).
				map(r -> ResponseEntity.ok(r));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<Void>> deleteRecommendations(Integer recommendationID, ServerWebExchange exchange) {
		
		return recommendationClient.delete().uri(uriFunction -> uriFunction.pathSegment(Api.RECOMMENDATION_PATH, String.valueOf(recommendationID)).build()).
				accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(Void.class).log().
				onErrorMap(WebClientResponseException.class, e -> handleException.handleHttpClientException(e)).
				map(v -> ResponseEntity.ok(v));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteRecommendationsAsync(Integer productID) {
		
		Event<Integer> event = new Event<>(productID, Event.Type.DELETE);
		log.info(" > A recommendation delete event will be sent : {}", event);
		messageProcessor.outputRecommendations().send(MessageBuilder.withPayload(event).build());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Health> getRecommendationHealth() {
		
		return recommendationClient.get().uri(uri -> uri.pathSegment(Actuator.BASE_PATH, Actuator.HEALTH_PATH).build()).
				retrieve().bodyToMono(String.class).map(s -> new Health.Builder().up().build()).
				onErrorResume(e -> Mono.just(new Health.Builder().down().build()));
	}
}
