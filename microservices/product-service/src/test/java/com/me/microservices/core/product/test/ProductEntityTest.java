package com.me.microservices.core.product.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;

import com.me.microservices.core.product.bo.ProductEntity;
import com.me.microservices.core.product.repository.ProductRepository;

import reactor.test.StepVerifier;

@DataMongoTest
@RunWith(SpringRunner.class)
public class ProductEntityTest {

	@Autowired
	private ProductRepository productRepository;
	
	private ProductEntity savedProduct;
	
	private static final Integer PRODUCT_ID = 1;
	private static final String PRODUCT_NAME = "MARTEAU_VERTUEUX";
	
	@Before
	public void setupdb() {
		
		StepVerifier.create(productRepository.deleteAll()).verifyComplete();
		
		ProductEntity productEntity = new ProductEntity(PRODUCT_ID, PRODUCT_NAME, 3);
		
		StepVerifier.create(productRepository.save(productEntity)).
			expectNextMatches(entity -> {savedProduct=entity;return areProductEqual(entity, productEntity);}).
			verifyComplete();
	}
	
	@Test
	public void create() {
		
		StepVerifier.create(productRepository.findByProductID(1)).
			expectNextMatches(entity -> areProductEqual(entity, savedProduct)).verifyComplete();
		
		ProductEntity newProductEntity = new ProductEntity(2, "EPEE_FLAMBOYANTE", 2);
		
		StepVerifier.create(productRepository.save(newProductEntity)).
			expectNextMatches(entity -> {savedProduct = entity; return areProductEqual(entity, newProductEntity);}).
			verifyComplete();
		
		StepVerifier.create(productRepository.findByProductID(2)).
			expectNextMatches(entity -> areProductEqual(entity, savedProduct)).
			verifyComplete();
	}
	
	@Test
	public void update() {
		
		StepVerifier.create(productRepository.findByProductID(PRODUCT_ID)).
			expectNextMatches(entity -> areProductEqual(entity, savedProduct)).
			verifyComplete();
		
		savedProduct.setName(savedProduct.getName() + "_FLAMBOYANTE");
		
		StepVerifier.create(productRepository.save(savedProduct)).
			expectNextMatches(entity -> areProductEqual(entity, savedProduct)).verifyComplete();
		
		StepVerifier.create(productRepository.findByProductID(1)).
			expectNextMatches(entity -> areProductEqual(entity, savedProduct)).
			verifyComplete();
	}
	
	@Test
	public void delete() {
		
		StepVerifier.create(productRepository.delete(savedProduct)).verifyComplete();
		StepVerifier.create(productRepository.existsById(savedProduct.getId())).expectNext(false).verifyComplete();
	}
	
	@Test
	public void findByProductId() {
		
		StepVerifier.create(productRepository.findByProductID(PRODUCT_ID)).
			expectNextMatches(entity -> areProductEqual(entity, savedProduct)).
			verifyComplete();
	}
	
	@Test
	public void productNotFound() {
		
		productRepository.delete(savedProduct).block();
		StepVerifier.create(productRepository.findByProductID(PRODUCT_ID)).verifyComplete();
	}
	
	@Test
	public void duplicateError() {
		
		ProductEntity entity = new ProductEntity(PRODUCT_ID, "MASSE", 1);
		StepVerifier.create(productRepository.save(entity)).expectError(DuplicateKeyException.class).verify();
	}
	
	@Test
	public void optimisticLockError() {
		
		ProductEntity productOne = productRepository.findByProductID(savedProduct.getProductID()).block();
		ProductEntity productTwo = productRepository.findByProductID(savedProduct.getProductID()).block();
		
		productOne.setName(productOne.getName() + "_updated_one");
		productRepository.save(productOne).block();
		
		productTwo.setName(productTwo.getName() + "_updated_two");
		StepVerifier.create(productRepository.save(productTwo)).
			expectError(OptimisticLockingFailureException.class).
			verify();
	}
	
	/**
	 * @param expectedProduct
	 * @param actualProduct
	 * @return True or False
	 */
	private boolean areProductEqual(ProductEntity expectedProduct, ProductEntity actualProduct) {
		
		return expectedProduct.getProductID() == actualProduct.getProductID() && 
				expectedProduct.getName().equals(actualProduct.getName()) &&
					expectedProduct.getWeight().equals(actualProduct.getWeight());
	}
}
