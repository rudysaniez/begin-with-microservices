package com.me.work.example.microservices.core.composite.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.me.work.example.api.Api;
import com.me.work.example.api.core.product.Product;
import com.me.work.example.api.core.product.ProductService;
import com.me.work.example.api.core.recommendation.Recommendation;
import com.me.work.example.api.core.recommendation.RecommendationService;
import com.me.work.example.api.core.review.Review;
import com.me.work.example.api.core.review.ReviewService;
import com.me.work.example.handler.exception.InvalidInputException;
import com.me.work.example.handler.exception.NotFoundException;
import com.me.work.example.handler.http.HttpErrorInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * @author rudysaniez @since 0.0.1
 */
@Slf4j
@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

	private final ObjectMapper jack;
	private final RestTemplate restTemplate;

	private final String productServiceUrl;
	private final String recommendationServiceUrl;
	private final String reviewServiceUrl;
	
	/**
	 * @param jack
	 * @param restTemplate
	 * @param productServiceHost
	 * @param productServicePort
	 * @param recommendationServiceHost
	 * @param recommendationServicePort
	 * @param reviewServiceHost
	 * @param reviewServicePort
	 * @param basePath
	 */
	@Autowired
	public ProductCompositeIntegration(ObjectMapper jack, RestTemplate restTemplate,
			@Value("${app.product-service.host}") String productServiceHost,
			@Value("${app.product-service.port}") int productServicePort,
			
			@Value("${app.recommendation-service.host}") String recommendationServiceHost,
			@Value("${app.recommendation-service.port}") int recommendationServicePort,
			
			@Value("${app.review-service.host}") String reviewServiceHost,
			@Value("${app.review-service.port}") int reviewServicePort,
			
			@Value("${spring.webflux.base-path}") String basePath) {
		
		this.jack = jack;
		this.restTemplate = restTemplate;
		
		if(log.isDebugEnabled())
			log.debug("Parameters : app.product-service.host={}, app.product-service.port={},"
					+ "app.recommendation-service.host={}, app.recommendation-service.port={},"
					+ "app.review-service.host={}, app.review-service.port={},"
					+ "spring.webflux.base-path={}", 
					productServiceHost, productServicePort,
					recommendationServiceHost, recommendationServicePort,
					reviewServiceHost, reviewServicePort,
					basePath);
		
		//Product-service
		StringBuilder sb = new StringBuilder("http://");
		sb.append(productServiceHost).append(":").append(productServicePort).append(basePath).
		append("/").append(Api.PRODUCT_PATH);
		
		this.productServiceUrl = sb.toString();
		
		if(log.isDebugEnabled())
			log.debug("L'URL du service product : " + this.productServiceUrl);
		
		
		//Recommendation-service
		sb = new StringBuilder("http://");
		sb.append(recommendationServiceHost).append(":").append(recommendationServicePort).append(basePath).
		append("/").append(Api.RECOMMENDATION_PATH);
		
		this.recommendationServiceUrl = sb.toString();
		
		if(log.isDebugEnabled())
			log.debug("L'URL du service recommendation : " + this.recommendationServiceUrl);
		
		
		//Review-service
		sb = new StringBuilder("http://");
		sb.append(reviewServiceHost).append(":").append(reviewServicePort).append(basePath).
		append("/").append(Api.REVIEW_PATH);
		
		this.reviewServiceUrl = sb.toString();
		
		if(log.isDebugEnabled())
			log.debug("L'URL du service review : " + this.reviewServiceUrl);
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Review getReview(String id) {
		
		Review out = null;
		
		try {
			
			out = this.restTemplate.getForObject(this.reviewServiceUrl + "/" + id, Review.class);
			
			if(out != null)
				log.debug("The review {} has been found", id);
			
		}
		catch(RestClientException rce) {
			log.error(rce.getMessage(), rce);
		}
		
		return out;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Review> getReviewByProductId(String productId) {
		
		List<Review> out = new ArrayList<>();
		
		try {
			
			out = this.restTemplate.exchange(this.reviewServiceUrl + "?productId=" + productId, HttpMethod.GET, 
					null, new ParameterizedTypeReference<List<Review>>() {}).getBody();  
			
			if(out != null && !out.isEmpty()) {
				
				if(log.isDebugEnabled())
					log.debug("{} reviews found", out.size());
			}
				
		}
		catch(RestClientException rce) {
			log.error(rce.getMessage(), rce);
		}
		
		return out;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Recommendation getRecommendation(String id) {
		
		Recommendation out = null;
		
		try {
			
			out = this.restTemplate.getForObject(this.recommendationServiceUrl + "/" + id, Recommendation.class);
			
			if(out != null) {
				
				if(log.isDebugEnabled())
					log.debug("The recommendation {} has been found", id);
			}
		}
		catch(RestClientException rce) {
			log.error(rce.getMessage(), rce);
		}
		
		return out;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Recommendation> getRecommendationByProductId(String productId) {
		
		List<Recommendation> out = new ArrayList<>();
		
		try {
			
			out = this.restTemplate.exchange(this.recommendationServiceUrl + "?productId=" + productId, HttpMethod.GET, 
					null, new ParameterizedTypeReference<List<Recommendation>>() {}).getBody();
			
			if(out != null && !out.isEmpty()) {
				
				if(log.isDebugEnabled())
					log.debug("{} recommendations found", out.size());
			}
		}
		catch(RestClientException rce) {
			log.error(rce.getMessage(), rce);
		}
		
		return out;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Product getProduct(String id) {
		
		try {
			
			Product product = this.restTemplate.getForObject(this.productServiceUrl + "/" + id, Product.class);
			
			if(log.isDebugEnabled())
				log.debug("The product {} has been found", id);
			
			return product;
		}
		catch(HttpClientErrorException ex) {
			
			switch(ex.getStatusCode()) {
			
				case NOT_FOUND:
					throw new NotFoundException(this.getMessage(ex));
					
				case UNPROCESSABLE_ENTITY:
					throw new InvalidInputException(this.getMessage(ex));
					
				default:
					log.warn("Got a unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
	                log.warn("Error body: {}", ex.getResponseBodyAsString());
					throw ex;
			}
		}
		
	}
	
	/**
	 * @param ex
	 * @return the error message
	 */
	private String getMessage(HttpClientErrorException ex) {
		
		try {
			HttpErrorInfo info = this.jack.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class);
			return info.getMessage();
		}
		catch(IOException io) {
			return ex.getMessage();
		}
	}
}
