package com.me.microservices.core.product.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.me.api.core.product.Product;
import com.me.microservices.core.product.bo.ProductEntity;

public interface ProductRepository extends MongoRepository<ProductEntity, String> {

	/**
	 * @param productID
	 * @return optional of {@link Product}
	 */
	public Optional<ProductEntity> findByProductID(int productID);
	
	/**
	 * @param name
	 * @return optional of {@link Product}
	 */
	public Optional<ProductEntity> findByName(String name);
	
	/**
	 * @param name
	 * @param page
	 * @return page of {@link Product}
	 */
	public Page<ProductEntity> findByNameStartingWith(String name, Pageable page);
}
