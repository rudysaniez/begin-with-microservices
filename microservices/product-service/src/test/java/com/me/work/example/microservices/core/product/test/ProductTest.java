package com.me.work.example.microservices.core.product.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.me.work.example.api.Api;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class ProductTest {

	@Autowired
	private WebTestClient client;
	
	@Value("${spring.webflux.base-path}") String basePath;
	
	@Test
	public void getProduct() {
		
		this.client.get().uri(this.basePath + "/" + Api.PRODUCT_PATH + "/1").accept(MediaType.APPLICATION_JSON).exchange().
			expectStatus().isOk().
			expectBody().jsonPath("$.productID").isEqualTo(Integer.valueOf(1)).
			jsonPath("$.name").isEqualTo("name-1");
	}
	
	@Test
	public void getProductNotFound() {
		
		this.client.get().uri(this.basePath + "/" + Api.PRODUCT_PATH + "/13").accept(MediaType.APPLICATION_JSON).exchange().
			expectStatus().is4xxClientError().
			expectBody().jsonPath("$.path").isEqualTo("/" + Api.PRODUCT_PATH + "/13").
			jsonPath("$.message").isEqualTo("The product 13 doesn't not exist");
	}
	
	@Test
	public void getProductInvalidInput() {
		
		this.client.get().uri(this.basePath + "/" + Api.PRODUCT_PATH + "/15").accept(MediaType.APPLICATION_JSON).exchange().
			expectStatus().is4xxClientError().
			expectBody().jsonPath("$.path").isEqualTo("/" + Api.PRODUCT_PATH + "/15").
			jsonPath("$.message").isEqualTo("The product 15 is an invalid input");
	}
}
