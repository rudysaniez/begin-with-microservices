package com.me.work.example.microservices.core.recommendation.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.me.work.example.microservices.core.recommendation.bo.RecommendationEntity;

public interface RecommendationRepository extends MongoRepository<RecommendationEntity, Integer> {

	/**
	 * @param recommendationID
	 * @return optional of {@link RecommendationEntity}
	 */
	public Optional<RecommendationEntity> findByRecommendationID(Integer recommendationID);
	
	/**
	 * @param productID
	 * @param page
	 * @return page of {@link RecommendationEntity}
	 */
	public Page<RecommendationEntity> findByProductID(Integer productID, Pageable page);
}
