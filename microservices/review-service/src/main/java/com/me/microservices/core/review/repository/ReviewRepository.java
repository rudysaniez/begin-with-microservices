package com.me.microservices.core.review.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.me.microservices.core.review.bo.ReviewEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReviewRepository extends ReactiveMongoRepository<ReviewEntity, String> {

	/**
	 * @param reviewID
	 * @return optional of {@link ReviewEntity}
	 */
	public Mono<ReviewEntity> findByReviewID(Integer reviewID);
	
	/**
	 * @param productID
	 * @param page
	 * @return {@link ReviewEntity}
	 */
	public Flux<ReviewEntity> findByProductID(Integer productID, Pageable page);
	
	/**
	 * @param productID
	 * @return
	 */
	public Mono<Long> countByProductID(Integer productID);
	
	/**
	 * @param reviewID
	 * @param productID
	 * @return {@link ReviewEntity}
	 */
	public Mono<ReviewEntity> findByReviewIDAndProductID(Integer reviewID, Integer productID);
	
	/**
	 * @param id
	 * @return True or False
	 */
	public Mono<Boolean> existsByReviewID(Integer id);
	
	/**
	 * @param reviewID
	 */
	public Mono<Void> deleteByReviewID(Integer reviewID);
	
	/**
	 * @param productID
	 */
	public Mono<Void> deleteByProductID(Integer productID);
}
