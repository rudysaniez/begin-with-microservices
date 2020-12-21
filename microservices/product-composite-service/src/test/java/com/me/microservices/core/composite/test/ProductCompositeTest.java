package com.me.microservices.core.composite.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;

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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.me.api.Api;
import com.me.handler.exception.InvalidInputException;
import com.me.handler.exception.NotFoundException;
import com.me.microservices.core.composite.Application.PaginationInformation;
import com.me.microservices.core.composite.builder.PagedRecommendationBuilder;
import com.me.microservices.core.composite.builder.PagedReviewBuilder;
import com.me.microservices.core.composite.builder.ProductBuilder;
import com.me.microservices.core.composite.integration.ProductIntegration;
import com.me.microservices.core.composite.integration.RecommendationIntegration;
import com.me.microservices.core.composite.integration.ReviewIntegration;
import com.me.microservices.core.recommendation.api.model.PagedRecommendation;
import com.me.microservices.core.review.api.model.PagedReview;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class ProductCompositeTest {

	@MockBean private ProductIntegration productIntegration;
	@MockBean private RecommendationIntegration recommendationIntegration;
	@MockBean private ReviewIntegration reviewIntegration;
	
	@Autowired private WebTestClient client;
	@Value("${spring.webflux.base-path}") private String basePath;
	@Autowired private PaginationInformation pagination;
	
	private static final Integer PRODUCT_ID = 1;
	private static final Integer RECOMMENDATION_ID = 1;
	private static final Integer REVIEW_ID = 1;
	private static final Integer PRODUCT_NOT_FOUND = 999;
	private static final Integer PRODUCT_INVALID_INPUT = 0;

	private static final String PRODUCT_NAME = "Panneau solaire";
	
	@Before
	public void setup() throws IOException {
		
		/**
		 * Micro service core : Product.
		 */
		when(productIntegration.getProduct(PRODUCT_ID, null)).
			thenReturn(Mono.just(ResponseEntity.ok(ProductBuilder.create().withProductID(PRODUCT_ID).withName(PRODUCT_NAME).withWeight(10).build())));
		
		/**
		 * Micro service core : Recommendation.
		 */
		
		PagedRecommendation pagedRecommendation = PagedRecommendationBuilder.create().withRecommendation(RECOMMENDATION_ID, PRODUCT_ID, "rudysaniez", 1, "This product is good!", LocalDateTime.now()).
			withPageMetadata(1L, 1L, 1L, 0L).build();
		
		when(recommendationIntegration.getRecommendationByProductId(PRODUCT_ID, pagination.getPageNumber(), pagination.getPageSize(), null)).
			thenReturn(Mono.just(ResponseEntity.ok(pagedRecommendation)));

		/**
		 * Mirco service core : Review.
		 */
		PagedReview pagedReview = PagedReviewBuilder.create().withReview(REVIEW_ID, PRODUCT_ID, "rudysaniez", "Good product", "This product is very good!", LocalDateTime.now()).
					withPageMetadata(1L, 1L, 1L, 0L).build();
		
		when(reviewIntegration.getReviewByProductId(PRODUCT_ID, pagination.getPageNumber(), pagination.getPageSize(), null)).
			thenReturn(Mono.just(ResponseEntity.ok(pagedReview)));

		/**
		 * Micro service core : Product not found.
		 */
		when(productIntegration.getProduct(PRODUCT_NOT_FOUND, null)).
			thenThrow(new NotFoundException(String.format("The product %d doesn't not exist", PRODUCT_NOT_FOUND)));
		
		/**
		 * Micro service core : Product invalid input.
		 */
		when(productIntegration.getProduct(PRODUCT_INVALID_INPUT, null)).
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
		
		StepVerifier.create(productIntegration.getProduct(PRODUCT_ID, null)).
			expectNextMatches(p -> p.getBody().getProductID().equals(PRODUCT_ID)).
			verifyComplete();
		
		PagedReview pageOfReview = reviewIntegration.getReviewByProductId(PRODUCT_ID, pagination.getPageNumber(), 
				pagination.getPageSize(), null).map(rs -> rs.getBody()).block();
		assertThat(pageOfReview.getContent()).isNotEmpty();
		
		
		PagedRecommendation pageOfRecommendation = recommendationIntegration.getRecommendationByProductId(PRODUCT_ID, pagination.getPageNumber(), 
				pagination.getPageSize(), null).map(rs -> rs.getBody()).block();
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
