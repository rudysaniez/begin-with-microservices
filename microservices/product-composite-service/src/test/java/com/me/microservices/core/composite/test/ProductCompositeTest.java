package com.me.microservices.core.composite.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.me.api.Api;
import com.me.api.core.common.PageMetadata;
import com.me.api.core.common.Paged;
import com.me.api.core.product.Product;
import com.me.api.core.recommendation.Recommendation;
import com.me.api.core.review.Review;
import com.me.handler.exception.InvalidInputException;
import com.me.handler.exception.NotFoundException;
import com.me.microservices.core.composite.Application.PaginationInformation;
import com.me.microservices.core.composite.services.ProductCompositeIntegration;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class ProductCompositeTest {

	@MockBean
	private ProductCompositeIntegration integration;
	
	@Autowired
	private WebTestClient client;
	
	@Autowired
	private PaginationInformation pagination;
	
	@Value("${spring.webflux.base-path}") 
	private String basePath;
	
	private static final Integer PRODUCT_ID = 1;
	private static final Integer RECOMMENDATION_ID = 1;
	private static final Integer REVIEW_ID = 1;
	private static final Integer PRODUCT_NOT_FOUND = 999;
	private static final Integer PRODUCT_INVALID_INPUT = 0;

	private static final String PRODUCT_NAME = "Panneau solaire";
	
	@Before
	public void setup() {
		
		/**
		 * Micro service core : Product.
		 */
		when(integration.getProduct(PRODUCT_ID)).
			thenReturn(Mono.just(new Product(PRODUCT_ID, PRODUCT_NAME, 10)));
		
		/**
		 * Micro service core : Recommendation.
		 */
		when(integration.getRecommendationByProductId(PRODUCT_ID, pagination.getPageNumber(), pagination.getPageSize())).
			thenReturn(Mono.just(new Paged<>(Collections.singletonList(new Recommendation(RECOMMENDATION_ID, PRODUCT_ID, "rudysaniez", 1, "This product is good!")), 
				new PageMetadata(1, 1, 1, 0))));

		/**
		 * Mirco service core : Review.
		 */
		when(integration.getReviewByProductId(PRODUCT_ID, pagination.getPageNumber(), pagination.getPageSize())).
			thenReturn(Mono.just(new Paged<>(Collections.singletonList(new Review(REVIEW_ID, PRODUCT_ID, "rudysaniez", "Good product", "This product is very good!")), 
				new PageMetadata(1, 1, 1, 0))));

		/**
		 * Micro service core : Product not found.
		 */
		when(this.integration.getProduct(PRODUCT_NOT_FOUND)).
			thenThrow(new NotFoundException(String.format("The product %d doesn't not exist", PRODUCT_NOT_FOUND)));
		
		/**
		 * Micro service core : Product invalid input.
		 */
		when(this.integration.getProduct(PRODUCT_INVALID_INPUT)).
			thenThrow(new InvalidInputException(String.format("The product %d is an invalid input", PRODUCT_INVALID_INPUT)));
	}
	
	@Test
	public void getCompositeProduct() {
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>(3);
		params.add("pageNumber", String.valueOf(pagination.getPageNumber()));
		params.add("pageSize", String.valueOf(pagination.getPageSize()));
		
		getAndVerifyStatus(PRODUCT_ID, HttpStatus.OK).
			jsonPath("$.name").isEqualTo(PRODUCT_NAME).
			jsonPath("$.recommendations.content[0].recommendationID").isEqualTo(1).
			jsonPath("$.reviews.content[0].reviewID").isEqualTo(1);
	}
	
	@Test
	public void getProductNotFoundException() {
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>(3);
		params.add("pageNumber", String.valueOf(pagination.getPageNumber()));
		params.add("pageSize", String.valueOf(pagination.getPageSize()));
		
		getAndVerifyStatus(PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND).
		jsonPath("$.message").isEqualTo(String.format("The product %d doesn't not exist", PRODUCT_NOT_FOUND));
	}
	
	@Test
	public void getProductInvalidInputException() {
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>(3);
		params.add("pageNumber", String.valueOf(pagination.getPageNumber()));
		params.add("pageSize", String.valueOf(pagination.getPageSize()));
		
		getAndVerifyStatus(PRODUCT_INVALID_INPUT, HttpStatus.UNPROCESSABLE_ENTITY);
	}
	
	@Test
	public void getMockedObjects() {
		
		StepVerifier.create(integration.getProduct(PRODUCT_ID)).expectNextMatches(p -> p.getProductID().equals(PRODUCT_ID)).
			verifyComplete();
		
		Paged<Review> pageOfReview = integration.getReviewByProductId(PRODUCT_ID, pagination.getPageNumber(), 
				pagination.getPageSize()).block();
		assertThat(pageOfReview.getContent()).isNotEmpty();
		
		
		Paged<Recommendation> pageOfRecommendation = integration.getRecommendationByProductId(PRODUCT_ID, pagination.getPageNumber(), 
				pagination.getPageSize()).block();
		assertThat(pageOfRecommendation.getContent()).isNotEmpty();
		
	}
	
	/**
	 * @param productID
	 * @param status
	 * @return {@link BodyContentSpec}
	 */
	private BodyContentSpec getAndVerifyStatus(Integer productID, HttpStatus status) {
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>(3);
		params.add("pageNumber", String.valueOf(pagination.getPageNumber()));
		params.add("pageSize", String.valueOf(pagination.getPageSize()));
		
		return client.get().uri(uri -> uri.pathSegment("api", "v1", Api.PRODUCT_COMPOSITE_PATH, productID.toString()).queryParams(params).build())
				.accept(MediaType.APPLICATION_JSON).exchange().
				expectStatus().isEqualTo(status).
				expectBody();
	}
}
