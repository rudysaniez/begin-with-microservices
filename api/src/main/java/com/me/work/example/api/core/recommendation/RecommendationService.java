package com.me.work.example.api.core.recommendation;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.me.work.example.api.Api;

public interface RecommendationService {

	/**
	 * @param id
	 * @return {@link Recommendation}
	 */
	@GetMapping(value=Api.RECOMMENDATION_PATH + "/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
	public Recommendation getRecommendation(@PathVariable(name="id", required=true) String id);
	
	/**
	 * @param productId
	 * @return list of {@link Recommendation}
	 */
	@GetMapping(value=Api.RECOMMENDATION_PATH, produces=MediaType.APPLICATION_JSON_VALUE)
	public List<Recommendation> getRecommendationByProductId(@RequestParam(name="productId", required=true) String productId);
}
