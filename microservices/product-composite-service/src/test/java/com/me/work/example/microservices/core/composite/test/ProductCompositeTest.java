package com.me.work.example.microservices.core.composite.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static reactor.core.publisher.Mono.just;

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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.me.work.example.api.Api;
import com.me.work.example.api.core.common.PageMetadata;
import com.me.work.example.api.core.common.Paged;
import com.me.work.example.api.core.composite.ProductComposite;
import com.me.work.example.api.core.composite.RecommendationSummary;
import com.me.work.example.api.core.composite.ReviewSummary;
import com.me.work.example.api.core.product.Product;
import com.me.work.example.api.core.recommendation.Recommendation;
import com.me.work.example.api.core.review.Review;
import com.me.work.example.handler.exception.InvalidInputException;
import com.me.work.example.handler.exception.NotFoundException;
import com.me.work.example.microservices.core.composite.Application.PaginationInformation;
import com.me.work.example.microservices.core.composite.services.ProductCompositeIntegration;

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
	private static final Integer PRODUCT_NOT_FOUND = 2;
	private static final Integer PRODUCT_INVALID_INPUT = 0;

	private static final String PRODUCT_NAME = "Panneau solaire";
	
	@Before
	public void setup() {
		
		when(this.integration.getProduct(PRODUCT_ID)).
			thenReturn(ResponseEntity.ok(new Product(PRODUCT_ID, PRODUCT_NAME, 10)));
		
		when(integration.getRecommendationByProductId(PRODUCT_ID, pagination.getPageNumber(), pagination.getPageSize())).
			thenReturn(ResponseEntity.ok(new Paged<>(Collections.singletonList(new Recommendation(1, PRODUCT_ID, "rudysaniez", 1, "This product is good!")), 
				new PageMetadata(1, 1, 1, 0))));

		when(integration.getReviewByProductId(PRODUCT_ID, pagination.getPageNumber(), pagination.getPageSize())).
			thenReturn(ResponseEntity.ok(new Paged<>(Collections.singletonList(new Review(1, PRODUCT_ID, "rudysaniez", "Good product", "This product is very good!")), 
				new PageMetadata(1, 1, 1, 0))));

		when(this.integration.getProduct(PRODUCT_NOT_FOUND)).
			thenThrow(new NotFoundException(String.format("The product %d doesn't not exist", PRODUCT_NOT_FOUND)));
		
		when(this.integration.getProduct(PRODUCT_INVALID_INPUT)).
			thenThrow(new InvalidInputException(String.format("The product %d is an invalid input", PRODUCT_INVALID_INPUT)));
	}
	
	@Test
	public void getProduct() {
		
		client.get().uri(uri -> uri.path(basePath + "/" + Api.PRODUCT_COMPOSITE_PATH + "/" + PRODUCT_ID).
				queryParam("pageNumber", pagination.getPageNumber()).queryParam("pageSize", pagination.getPageSize()).build()).
			accept(MediaType.APPLICATION_JSON).exchange().
				expectStatus().is2xxSuccessful().
				expectBody().
					jsonPath("$.name").isEqualTo(PRODUCT_NAME).
					jsonPath("$.recommendations.content[0].recommendationID").isEqualTo(1).
					jsonPath("$.reviews.content[0].reviewID").isEqualTo(1);
	}
	
	@Test
	public void saveProduct() {
		
		ProductComposite productComposite = new ProductComposite(PRODUCT_ID, PRODUCT_NAME, 1, 
				Collections.singletonList(new RecommendationSummary(1, "rsaniez", 1, "Very good!")), 
				Collections.singletonList(new ReviewSummary(1, "rsaniez", "My opinion", "I am very happy...")));
		
		client.post().uri(basePath + "/" + Api.PRODUCT_COMPOSITE_PATH).body(just(productComposite), ProductComposite.class).
			accept(MediaType.APPLICATION_JSON).exchange().
				expectStatus().isEqualTo(HttpStatus.CREATED);
	}
	
	@Test
	public void deleteProduct() {
		
		ResponseEntity<Paged<Recommendation>> recommendations = integration.getRecommendationByProductId(PRODUCT_ID, 0, 20);
		assertEquals(recommendations.getBody().getContent().stream().findFirst().isPresent(), true);
		
		ResponseEntity<Paged<Review>> reviews = integration.getReviewByProductId(PRODUCT_ID, 0, 20);
		assertEquals(reviews.getBody().getContent().stream().findFirst().isPresent(), true);
		
		client.delete().uri(basePath + "/" + Api.PRODUCT_COMPOSITE_PATH + "/" + PRODUCT_ID).exchange().
			expectStatus().isEqualTo(HttpStatus.OK);
	}
	
	@Test
	public void productNotFound() {
		
		client.get().uri(basePath + "/" + Api.PRODUCT_COMPOSITE_PATH + "/" + PRODUCT_NOT_FOUND).
			accept(MediaType.APPLICATION_JSON).exchange().
				expectStatus().isEqualTo(HttpStatus.NOT_FOUND).
					expectBody().
						jsonPath("$.message").isEqualTo(String.format("The product %d doesn't not exist", PRODUCT_NOT_FOUND));
	}
	
	@Test
	public void productInvalidInput() {
		
		client.get().uri(basePath + "/" + Api.PRODUCT_COMPOSITE_PATH + "/" + PRODUCT_INVALID_INPUT).
			accept(MediaType.APPLICATION_JSON).exchange().
				expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY).
				expectBody().
					jsonPath("$.message").isEqualTo(String.format("The product %d is an invalid input", PRODUCT_INVALID_INPUT));
	}
}
