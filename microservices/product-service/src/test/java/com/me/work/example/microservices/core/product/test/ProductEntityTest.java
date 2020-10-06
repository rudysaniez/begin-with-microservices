package com.me.work.example.microservices.core.product.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

import com.me.work.example.microservices.core.product.bo.ProductEntity;
import com.me.work.example.microservices.core.product.repository.ProductRepository;

@DataMongoTest
@RunWith(SpringRunner.class)
public class ProductEntityTest {

	@Autowired
	private ProductRepository productRepository;
	
	private ProductEntity savedProduct;
	
	@Before
	public void setupdb() {
		
		productRepository.deleteAll();
		
		ProductEntity productEntity = new ProductEntity(1, "Marteau vertueux", 3);
		savedProduct = productRepository.save(productEntity);
		assertEqualsProduct(productEntity, savedProduct);
	}
	
	@Test
	public void create() {
		
		ProductEntity productEntity = productRepository.findByProductID(1).get();
		assertEqualsProduct(savedProduct, productEntity);
		
		ProductEntity newProductEntity = new ProductEntity(2, "Epée de l'espoir", 2);
		productRepository.save(newProductEntity);
		assertEquals(2L, productRepository.count());
		
		ProductEntity foundProduct = productRepository.findByProductID(2).get();
		assertEqualsProduct(newProductEntity, foundProduct);
	}
	
	@Test
	public void update() {
		
		savedProduct = productRepository.findByProductID(1).get();
		savedProduct.setName(savedProduct.getName() + " et de lumière");
		productRepository.save(savedProduct);
		
		ProductEntity foundProduct = productRepository.findByProductID(1).get();
		assertEqualsProduct(savedProduct, foundProduct);
		assertEquals(1, foundProduct.getVersion());
		assertEquals(savedProduct.getName(), foundProduct.getName());
	}
	
	@Test
	public void delete() {
		
		ProductEntity productEntity = new ProductEntity(2, "Epée de la justice", 2);
		productEntity = productRepository.save(productEntity);
		
		assertTrue(productRepository.findByProductID(2).isPresent());
		productRepository.delete(productEntity);
		assertFalse(productRepository.findByProductID(2).isPresent());
	}
	
	@Test
	public void findByProductId() {
		
		Optional<ProductEntity> optOfProduct = productRepository.findByProductID(1);
		assertTrue(optOfProduct.isPresent());
		assertEqualsProduct(savedProduct, optOfProduct.get());
	}
	
	@Test(expected=DuplicateKeyException.class)
	public void duplicateError() {
		
		assertTrue(productRepository.findByProductID(savedProduct.getProductID()).isPresent());
		
		ProductEntity productEntity = new ProductEntity(savedProduct.getProductID(), "Sabre lumineux", 2);
		productEntity = productRepository.save(productEntity);
	}
	
	@Test(expected=OptimisticLockingFailureException.class)
	public void optimisticLockError() {
		
		ProductEntity productOne = productRepository.findByProductID(savedProduct.getProductID()).get();
		ProductEntity productTwo = productRepository.findByProductID(savedProduct.getProductID()).get();
		
		productOne.setName(productOne.getName() + "_updated_one");
		productOne = productRepository.save(productOne);
		
		productTwo.setName(productTwo.getName() + "_updated_two");
		productTwo = productRepository.save(productTwo);
	}
	
	@Test
	public void paging() {
		
		productRepository.delete(savedProduct);
		
		List<ProductEntity> products = IntStream.rangeClosed(1000, 1030).mapToObj(i -> new ProductEntity(i, "Epée de la Justice v" + (i - 1000), (i - 1000))).
			collect(Collectors.toList());
		
		productRepository.saveAll(products);
		
		Pageable page = PageRequest.of(0, 4, Direction.ASC, "productID");
		
		page = assertNextPage(page, "[1000, 1001, 1002, 1003]");
		page = assertNextPage(page.next(), "[1004, 1005, 1006, 1007]");
		page = assertNextPage(page.next(), "[1008, 1009, 1010, 1011]");
	}
	
	/**
	 * @param page
	 * @param listOfProductIDs
	 * @return {@link Pageable}
	 */
	private Pageable assertNextPage(Pageable page, String listOfProductIDs) {
		
		Page<ProductEntity> pageOfproducts = productRepository.findAll(page);
		String IDs = pageOfproducts.stream().map(pe -> pe.getProductID()).collect(Collectors.toList()).toString();
		assertEquals(IDs, listOfProductIDs);
		
		return page;
	}
	
	/**
	 * @param expectedProduct
	 * @param actualProduct
	 */
	private void assertEqualsProduct(ProductEntity expectedProduct, ProductEntity actualProduct) {
		
		assertEquals(expectedProduct.getName(), actualProduct.getName());
		assertEquals(expectedProduct.getWeight(), actualProduct.getWeight());
		assertEquals(expectedProduct.getProductID(), actualProduct.getProductID());
		assertEquals(expectedProduct.getVersion(), actualProduct.getVersion());
	}
}
