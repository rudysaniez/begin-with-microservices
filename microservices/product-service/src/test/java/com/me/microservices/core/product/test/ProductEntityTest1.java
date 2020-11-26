package com.me.microservices.core.product.test;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import com.me.handler.exception.NotFoundException;
import com.me.microservices.core.product.bo.ProductEntity;
import com.me.microservices.core.product.repository.ProductRepository;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Ignore
@DataMongoTest
@RunWith(SpringRunner.class)
public class ProductEntityTest1 {

	@Autowired
	private ProductRepository productRepository;
	
	private static final Integer PRODUCT_ID = 1;
	private static final String PRODUCT_NAME = "MARTEAU_VERTUEUX";
	private static final Integer PRODUCT_WEIGHT = 1;
	
	@Before
	public void before() {
		
		productRepository.deleteAll().block();
		
		ProductEntity productEntity = new ProductEntity(PRODUCT_ID, PRODUCT_NAME, PRODUCT_WEIGHT);
		
		StepVerifier.create(productRepository.save(productEntity)).
			expectNextMatches(entity -> entity.getName().equals(PRODUCT_NAME)).
			verifyComplete();
	}
	
	@Test
	public void findProductByStartingName() {
		
		StepVerifier.create(productRepository.findByNameStartingWith("MART", PageRequest.of(0, 10)).collectList()).
		expectNextMatches(list -> list.size() == 1).verifyComplete();
			
	}
	
	@Test
	public void findByProductId() {
		
		StepVerifier.create(productRepository.findByProductID(PRODUCT_ID)).
			expectNextMatches(entity -> entity.getProductID() == PRODUCT_ID).verifyComplete();
	}
	
	@Test
	public void updateProduct() {
		
		StepVerifier.create(  
				
			productRepository.findByProductID(PRODUCT_ID).
				switchIfEmpty(Mono.error(new NotFoundException())).
				map(entity -> {
					entity.setName("MARTEAU_ARGENT");
					return entity;
				}).
				flatMap(entity -> productRepository.save(entity))
			
		).expectNextMatches(entity -> entity.getName().equals("MARTEAU_ARGENT")).verifyComplete();
		
		StepVerifier.create(productRepository.findByName("MARTEAU_ARGENT")).
			expectNextMatches(entity -> entity.getProductID() == PRODUCT_ID).verifyComplete();
	}
}
