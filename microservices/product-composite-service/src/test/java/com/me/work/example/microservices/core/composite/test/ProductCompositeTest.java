package com.me.work.example.microservices.core.composite.test;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.me.work.example.api.Api;
import com.me.work.example.api.core.product.Product;
import com.me.work.example.api.core.recommendation.Recommendation;
import com.me.work.example.api.core.review.Review;
import com.me.work.example.handler.exception.InvalidInputException;
import com.me.work.example.handler.exception.NotFoundException;
import com.me.work.example.microservices.core.composite.services.ProductCompositeIntegration;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class ProductCompositeTest {

	@MockBean
	private ProductCompositeIntegration integration;
	
	
	@Autowired
	private WebTestClient client;
	
	@Value("${spring.webflux.base-path}") String basePath;
	
	@Before
	public void setup() {
		
		when(this.integration.getProduct("1")).thenReturn(new Product("1", "Panneau solaire", 10));
		
		when(this.integration.getRecommendationByProductId("1")).thenReturn(
				singletonList(new Recommendation("1", "1", "rudysaniez", 1, "VALIDATED")));
		
		when(this.integration.getReviewByProductId("1")).thenReturn(
				singletonList(new Review("1", "1", "rudysaniez", "Good product", "VALIDATED")));
		
		when(this.integration.getProduct("13")).thenThrow(
				new NotFoundException(String.format("The product %s doesn't not exist", "13")));
		
		when(this.integration.getProduct("15")).thenThrow(
				new InvalidInputException(String.format("The product %s is an invalid input", "15")));
		
	}
	
	@Test
	public void getProduct() {
		
		this.client.get().uri(basePath + "/" + Api.PRODUCT_COMPOSITE_PATH + "/1").accept(MediaType.APPLICATION_JSON).exchange().
			expectStatus().is2xxSuccessful().
			expectBody().jsonPath("$.product.name").isEqualTo("Panneau solaire");
	}
	
	@Test
	public void getProductNotFound() {
		
		this.client.get().uri(basePath + "/" + Api.PRODUCT_COMPOSITE_PATH + "/13").accept(MediaType.APPLICATION_JSON).exchange().
			expectStatus().isNotFound().
			expectBody().jsonPath("$.message").isEqualTo("The product 13 doesn't not exist");
	}
	
	@Test
	public void getProductInvalidInput() {
		
		this.client.get().uri(basePath + "/" + Api.PRODUCT_COMPOSITE_PATH + "/15").accept(MediaType.APPLICATION_JSON).exchange().
			expectStatus().is4xxClientError().
			expectBody().jsonPath("$.message").isEqualTo("The product 15 is an invalid input");
	}
}
