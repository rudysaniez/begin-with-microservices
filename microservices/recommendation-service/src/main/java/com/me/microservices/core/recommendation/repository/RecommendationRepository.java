package com.me.microservices.core.recommendation.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.me.microservices.core.recommendation.bo.RecommendationEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RecommendationRepository extends ReactiveMongoRepository<RecommendationEntity, String> {

	/**
	 * @param recommendationID
	 * @return optional of {@link RecommendationEntity}
	 */
	public Mono<RecommendationEntity> findByRecommendationID(Integer recommendationID);
	
	/**
	 * @param productID
	 * @param page
	 * @return page of {@link RecommendationEntity}
	 */
	public Flux<RecommendationEntity> findByProductID(Integer productID, Pageable page);
	
	/**
	 * @param productID
	 * @return mono of {@link Long}
	 */
	public Mono<Long> countByProductID(Integer productID);
}
