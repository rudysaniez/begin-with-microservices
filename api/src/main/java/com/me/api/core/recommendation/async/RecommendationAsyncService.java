package com.me.api.core.recommendation.async;

public interface RecommendationAsyncService {

	/**
	 * @param productID
	 */
	public void deleteRecommendationsAsync(Integer productID);
}
