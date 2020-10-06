package com.me.work.example.microservices.core.product.test;

import static org.junit.Assert.assertTrue;
import static reactor.core.publisher.Mono.just;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.me.work.example.api.Api;
import com.me.work.example.api.core.product.Product;
import com.me.work.example.microservices.core.product.bo.ProductEntity;
import com.me.work.example.microservices.core.product.mapper.ProductMapper;
import com.me.work.example.microservices.core.product.repository.ProductRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class ProductServiceTest {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private ProductMapper mapper;
	
	@Value("${spring.webflux.base-path}") String basePath;
	
	private static final String PRODUCT_NAME = "marteau vertueux";
	private static final Integer PRODUCT_ID = 999;
	private static final Integer PRODUCT_WEIGHT = 999;
	
	@Before
	public void setupdb() {
		
		productRepository.deleteAll();
		ProductEntity productEntity = new ProductEntity(PRODUCT_ID, PRODUCT_NAME, PRODUCT_WEIGHT);
		
		client.post().uri(basePath + "/" + Api.PRODUCT_PATH).accept(MediaType.APPLICATION_JSON).
			body(just(mapper.toModel(productEntity)), Product.class).exchange().
			expectStatus().isEqualTo(HttpStatus.CREATED).
			expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody().
				jsonPath("$.name").isEqualTo(PRODUCT_NAME.toUpperCase());
		
		assertTrue(productRepository.findByProductID(PRODUCT_ID).isPresent());
	}
	
	@Test
	public void getProduct() {
		
		client.get().uri(basePath + "/" + Api.PRODUCT_PATH + "/" + PRODUCT_ID).accept(MediaType.APPLICATION_JSON).exchange().
			expectStatus().isOk().
			expectHeader().contentType(MediaType.APPLICATION_JSON).
			expectBody().
				jsonPath("$.productID").isEqualTo(PRODUCT_ID).
				jsonPath("$.name").isEqualTo(PRODUCT_NAME.toUpperCase()).
				jsonPath("$.productID").isEqualTo(PRODUCT_ID);
	}
	
	@Test
	public void getProductNotFound() {
		
		client.get().uri(basePath + "/" + Api.PRODUCT_PATH + "/13").accept(MediaType.APPLICATION_JSON).exchange().
			expectStatus().isEqualTo(HttpStatus.NOT_FOUND).
			expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE).
			expectBody().
				jsonPath("$.path").isEqualTo("/" + Api.PRODUCT_PATH + "/13").
				jsonPath("$.message").isEqualTo("The product with productID=13 doesn't not exists.");
	}
	
	@Test
	public void getProductWithInvalidProductID() {
		
		client.get().uri(basePath + "/" + Api.PRODUCT_PATH + "/0").accept(MediaType.APPLICATION_JSON).exchange().
			expectStatus().is4xxClientError().
			expectBody().
				jsonPath("$.path").isEqualTo("/" + Api.PRODUCT_PATH + "/0").
				jsonPath("$.message").isEqualTo("ProductID should be greater than 0");
	}
	
	@Test
	public void findProductByName() {
		
		client.get().uri(String.format(basePath + "/" + Api.PRODUCT_PATH + "?name=%s&", "marte")).accept(MediaType.APPLICATION_JSON).exchange().
			expectStatus().isEqualTo(HttpStatus.OK).
			expectBody().
				jsonPath("$.content[0].productID").isEqualTo(PRODUCT_ID).
				jsonPath("$.content[0].name").isEqualTo(PRODUCT_NAME.toUpperCase());
	}
	
	@Test
	public void saveProduct() {
		
		ProductEntity productEntity = new ProductEntity(1, "epee de la justice", 3);
		
		client.post().uri(basePath + "/" + Api.PRODUCT_PATH).accept(MediaType.APPLICATION_JSON).
			body(just(mapper.toModel(productEntity)), Product.class).exchange().
				expectStatus().isEqualTo(HttpStatus.CREATED).
				expectBody().jsonPath("$.productID").isEqualTo(1);
	}
	
	@Test
	public void saveProductDuplicateKey() {
		
		ProductEntity productEntity = new ProductEntity(PRODUCT_ID, "Sabre de lumiere", 2);
		
		client.post().uri(basePath + "/" + Api.PRODUCT_PATH).contentType(MediaType.APPLICATION_JSON).
			body(just(mapper.toModel(productEntity)), Product.class).exchange().
				expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY).
				expectBody().
					jsonPath("$.message").isEqualTo(String.format("Duplicate key : check the productID (%d) or the name (%s) of product.", PRODUCT_ID, "Sabre de lumiere"));
	}
	
	@Test
	public void updateProduct() {
		
		ProductEntity productEntity = new ProductEntity(PRODUCT_ID, "Sabre de lumiere", 2);
		
		client.put().uri(basePath + "/" + Api.PRODUCT_PATH + "/" + PRODUCT_ID).contentType(MediaType.APPLICATION_JSON).
			body(just(mapper.toModel(productEntity)), Product.class).exchange().
				expectStatus().isEqualTo(HttpStatus.OK).
				expectBody().
					jsonPath("$.name").isEqualTo("Sabre de lumiere".toUpperCase());
	}
	
	@Test
	public void deleteProduct() {
		
		client.delete().uri(basePath + "/" + Api.PRODUCT_PATH + "/" + PRODUCT_ID).exchange().
			expectStatus().isEqualTo(HttpStatus.OK);
	}
}
