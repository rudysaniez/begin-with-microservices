package com.me.api.core.recommendation;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.me.api.Api;
import com.me.api.core.common.Paged;

import reactor.core.publisher.Mono;

public interface RecommendationService {

	/**
	 * @param recommendationID
	 * @return {@link Recommendation}
	 */
	@GetMapping(value=Api.RECOMMENDATION_PATH + "/{recommendationID}", produces=MediaType.APPLICATION_JSON_VALUE)
	public Mono<Recommendation> getRecommendation(@PathVariable(name="recommendationID", required=true) Integer recommendationID);
	
	/**
	 * @param productId
	 * @param pageNumber
	 * @param pageSize
	 * @return page of {@link Recommendation}
	 */
	@GetMapping(value=Api.RECOMMENDATION_PATH, produces=MediaType.APPLICATION_JSON_VALUE)
	public Mono<Paged<Recommendation>> getRecommendationByProductId(@RequestParam(name="productId", required=true) Integer productID,
			@RequestParam(name="pageNumber", required=false) Integer pageNumber, 
				@RequestParam(name="pageSize", required=false) Integer pageSize);

	/**
	 * @param recommendation
	 * @return {@link Recommendation}
	 */
	@PostMapping(value=Api.RECOMMENDATION_PATH, produces=MediaType.APPLICATION_JSON_VALUE, consumes=MediaType.APPLICATION_JSON_VALUE)
	public Mono<Recommendation> save(@RequestBody Recommendation recommendation);
	
	/**
	 * @param recommendation
	 * @param recommendationID
	 * @return {@link Recommendation}
	 */
	@PutMapping(value=Api.RECOMMENDATION_PATH + "/{recommendationID}", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public Mono<Recommendation> update(@RequestBody Recommendation recommendation, @PathVariable(name="recommendationID", required=true) Integer recommendationID);
	
	/**
	 * @param recommendationID
	 */
	@DeleteMapping(value=Api.RECOMMENDATION_PATH + "/{recommendationID}")
	public Mono<Void> deleteRecommendation(@PathVariable(name="recommendationID", required=true) Integer recommendationID);
}
