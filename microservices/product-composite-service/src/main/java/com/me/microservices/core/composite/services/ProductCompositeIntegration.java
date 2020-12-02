package com.me.microservices.core.composite.services;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.me.api.Api;
import com.me.api.core.common.Paged;
import com.me.api.core.product.Product;
import com.me.api.core.product.ProductService;
import com.me.api.core.product.async.ProductAsyncService;
import com.me.api.core.recommendation.Recommendation;
import com.me.api.core.recommendation.RecommendationService;
import com.me.api.core.recommendation.async.RecommendationAsyncService;
import com.me.api.core.review.Review;
import com.me.api.core.review.ReviewService;
import com.me.api.core.review.async.ReviewAsyncService;
import com.me.api.event.Event;
import com.me.handler.exception.InvalidInputException;
import com.me.handler.exception.NotFoundException;
import com.me.handler.http.HttpErrorInfo;
import com.me.microservices.core.composite.producer.MessageProcessor;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * @author rudysaniez @since 0.0.1
 */
@Slf4j
@Component
public class ProductCompositeIntegration implements ProductService, ProductAsyncService, 
													RecommendationService, RecommendationAsyncService, 
													ReviewService, ReviewAsyncService {

	private final ObjectMapper jack;
	private final WebClient productClient;
	private final WebClient recommendationClient;
	private final WebClient reviewClient;
	private final MessageProcessor messageProcessor;

	@Autowired
	public ProductCompositeIntegration(ObjectMapper jack, WebClient.Builder webClientBuilder, MessageProcessor messageProcessor,
			
			@Value("${app.product-service.host}") String productServiceHost,
			@Value("${app.product-service.port}") int productServicePort,
			
			@Value("${app.recommendation-service.host}") String recommendationServiceHost,
			@Value("${app.recommendation-service.port}") int recommendationServicePort,
			
			@Value("${app.review-service.host}") String reviewServiceHost,
			@Value("${app.review-service.port}") int reviewServicePort,
			
			@Value("${spring.webflux.base-path}") String basePath) {
		
		this.jack = jack;
		this.messageProcessor = messageProcessor;
		
		/**
		 * Product url configuration.
		 */
		String productServiceUrl = new StringBuilder("http://").
				append(productServiceHost).append(":").append(productServicePort).
				append(basePath).toString();
		
		log.debug(" > Product service : " + productServiceUrl);
		
		
		/**
		 * Recommendation url configuration.
		 */
		String recommendationServiceUrl = new StringBuilder("http://").
				append(recommendationServiceHost).append(":").append(recommendationServicePort).
				append(basePath).toString();
		
		log.debug(" > Recommendation service : " + recommendationServiceUrl);
		
		
		/**
		 * Review url configuration.
		 */
		String reviewServiceUrl =new StringBuilder("http://").
				append(reviewServiceHost).append(":").append(reviewServicePort).
				append(basePath).toString();
		
		log.debug(" > Review service : " + reviewServiceUrl);
		
		/**
		 * WebClient.
		 */
		this.productClient = webClientBuilder.uriBuilderFactory(new DefaultUriBuilderFactory(productServiceUrl)).build();
		this.recommendationClient = webClientBuilder.uriBuilderFactory(new DefaultUriBuilderFactory(recommendationServiceUrl)).build();
		this.reviewClient = webClientBuilder.uriBuilderFactory(new DefaultUriBuilderFactory(reviewServiceUrl)).build();
	}
	
	/**
	 * Review implementation part.
	 */
	
	/**
	 * {@inheritDocy}
	 */
	@Override
	public Mono<Review> getReview(Integer reviewID) {
		
		return reviewClient.get().uri(uriFunction -> uriFunction.pathSegment(Api.REVIEW_PATH, String.valueOf(reviewID)).build()).
				accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(Review.class).log().
				onErrorMap(WebClientResponseException.class, e -> handleHttpClientException(e));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Paged<Review>> getReviewByProductId(Integer productID, Integer pageNumber, Integer pageSize) {
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>(3);
		params.add("productId", String.valueOf(productID));
		
		if(pageNumber != null && pageSize != null) {
			
			params.add("pageNumber", String.valueOf(pageNumber));
			params.add("pageSize", String.valueOf(pageSize));
		}
		
		return reviewClient.get().uri(uri -> uri.pathSegment(Api.REVIEW_PATH).queryParams(params).build()).
				accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(new ParameterizedTypeReference<Paged<Review>>() {}).log().
				onErrorResume(e -> Mono.empty());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Review> save(Review review) {
		
		return reviewClient.post().uri(uri -> uri.pathSegment(Api.REVIEW_PATH).build()).
				body(Mono.just(review), Review.class).accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(Review.class).log().onErrorMap(WebClientResponseException.class, e -> handleHttpClientException(e));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Review> update(Review review, Integer reviewID) {
		
		return reviewClient.put().uri(uriFunction -> uriFunction.pathSegment(Api.REVIEW_PATH, String.valueOf(reviewID)).build()).
				body(Mono.just(review), Review.class).accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(Review.class).log().
				onErrorMap(WebClientResponseException.class, e -> handleHttpClientException(e));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Void> deleteReviews(Integer reviewID) {
		
		return reviewClient.delete().uri(uriFunction -> uriFunction.pathSegment(Api.REVIEW_PATH, String.valueOf(reviewID)).build()).
				retrieve().bodyToMono(Void.class).log().
				onErrorMap(WebClientResponseException.class, e -> handleHttpClientException(e));
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
	 * Recommendation implementation part.
	 */
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Recommendation> getRecommendation(Integer recommendationID) {
		
		return recommendationClient.get().uri(uriFunction -> uriFunction.pathSegment(Api.RECOMMENDATION_PATH, String.valueOf(recommendationID)).build()).
				accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Recommendation.class).log().
				onErrorMap(WebClientResponseException.class, e -> handleHttpClientException(e));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Paged<Recommendation>> getRecommendationByProductId(Integer productID, Integer pageNumber, Integer pageSize) {
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>(3);
		params.add("productId", String.valueOf(productID));
		
		if(pageNumber != null && pageSize != null) {
			
			params.add("pageNumber", String.valueOf(pageNumber));
			params.add("pageSize", String.valueOf(pageSize));
		}
		
		return recommendationClient.get().uri(uriFunction -> uriFunction.pathSegment(Api.RECOMMENDATION_PATH).queryParams(params).build()).
				accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(new ParameterizedTypeReference<Paged<Recommendation>>() {}).log().
				onErrorResume(e -> Mono.empty());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Recommendation> save(Recommendation recommendation) {
		
		return recommendationClient.post().uri(uri -> uri.pathSegment(Api.RECOMMENDATION_PATH).build()).
				body(Mono.just(recommendation), Recommendation.class).accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(Recommendation.class).log().
				onErrorMap(WebClientResponseException.class, e -> handleHttpClientException(e));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Recommendation> update(Recommendation recommendation, Integer recommendationID) {
		
		return recommendationClient.put().uri(uriFunction -> uriFunction.pathSegment(Api.RECOMMENDATION_PATH, String.valueOf(recommendationID)).build()).
				body(Mono.just(recommendation), Recommendation.class).accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(Recommendation.class).log().
				onErrorMap(WebClientResponseException.class, e -> handleHttpClientException(e));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Void> deleteRecommendations(Integer recommendationID) {
		
		return recommendationClient.delete().uri(uriFunction -> uriFunction.pathSegment(Api.RECOMMENDATION_PATH, String.valueOf(recommendationID)).build()).
				accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(Void.class).log().
				onErrorMap(WebClientResponseException.class, e -> handleHttpClientException(e));
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
	 * Product implementation part.
	 */
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Product> getProduct(Integer productID) {
		
		return productClient.get().uri(uriFunction -> uriFunction.pathSegment(Api.PRODUCT_PATH, productID.toString()).build()).
				accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(Product.class).log().
				onErrorMap(WebClientResponseException.class, e -> handleHttpClientException(e));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Paged<Product>> findByName(String name, Integer pageNumber, Integer pageSize) {
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>(3);
		params.add("name", name);
		
		if(pageNumber != null && pageSize != null) {
			
			params.add("pageNumber", String.valueOf(pageNumber));
			params.add("pageSize", String.valueOf(pageSize));
		}
		
		return productClient.get().uri(uriFunction -> uriFunction.pathSegment(Api.PRODUCT_PATH).queryParams(params).build()).
				accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(new ParameterizedTypeReference<Paged<Product>>() {}).log().
				onErrorMap(WebClientResponseException.class, e -> handleHttpClientException(e));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Product> save(Product product) {
		
		return productClient.post().uri(uriFunction -> uriFunction.pathSegment(Api.PRODUCT_PATH).build()).
				body(Mono.just(product), Product.class).accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(Product.class).log().
				onErrorMap(WebClientResponseException.class, e -> handleHttpClientException(e));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Product> update(Product product, Integer productID) {
		
		return productClient.put().uri(uriFunction -> uriFunction.pathSegment(Api.PRODUCT_PATH, String.valueOf(productID)).build()).
				body(Mono.just(product), Product.class).accept(MediaType.APPLICATION_JSON).retrieve().
				bodyToMono(Product.class).log().
				onErrorMap(WebClientResponseException.class, e -> handleHttpClientException(e));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Void> deleteProduct(Integer productID) {
		
		return productClient.delete().uri(uriFunction -> uriFunction.pathSegment(Api.PRODUCT_PATH, String.valueOf(productID)).build()).
				accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Void.class).log().
				onErrorMap(WebClientResponseException.class, e -> handleHttpClientException(e));
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
	 * @param ex
	 * @return {@link RuntimeException}
	 */
	private Throwable handleHttpClientException(WebClientResponseException e) {
		
		switch(e.getStatusCode()) {
		
			case NOT_FOUND:
				return new NotFoundException(getMessage(e));
	
			case UNPROCESSABLE_ENTITY:
				return new InvalidInputException(getMessage(e));
				
			default:
				log.warn("Got a unexpected http error: {}", e.getStatusCode());
				log.warn("{}", e.getResponseBodyAsString());
				return e;
		}
	}
	
	/**
	 * @param ex
	 * @return the error message
	 */
	private String getMessage(WebClientResponseException ex) {
		
		try {
			HttpErrorInfo info = jack.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class);
			return info.getMessage();
		}
		catch(IOException io) {
			return ex.getMessage();
		}
	}
}
