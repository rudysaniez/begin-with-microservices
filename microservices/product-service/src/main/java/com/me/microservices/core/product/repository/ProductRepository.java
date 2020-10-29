package com.me.microservices.core.product.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.me.api.core.product.Product;
import com.me.microservices.core.product.bo.ProductEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository extends ReactiveMongoRepository<ProductEntity, String> {

	/**
	 * @param productID
	 * @return mono of {@link Product}
	 */
	public Mono<ProductEntity> findByProductID(int productID);
	
	/**
	 * @param name
	 * @return mono of {@link Product}
	 */
	public Mono<ProductEntity> findByName(String name);
	
	/**
	 * @param name
	 * @param page
	 * @return flux of {@link Product}
	 */
	public Flux<ProductEntity> findByNameStartingWith(String name, Pageable page);
	
	/**
	 * @param name
	 * @return mono of {@link Long}
	 */
	public Mono<Long> countByNameStartingWith(String name);
}
