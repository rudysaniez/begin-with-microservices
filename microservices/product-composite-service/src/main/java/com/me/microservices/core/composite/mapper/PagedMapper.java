package com.me.microservices.core.composite.mapper;

import org.mapstruct.Mapper;

import com.me.microservices.core.productcomposite.api.model.PagedRecommendationSummary;
import com.me.microservices.core.productcomposite.api.model.PagedReviewSummary;
import com.me.microservices.core.recommendation.api.model.PagedRecommendation;
import com.me.microservices.core.review.api.model.PagedReview;

@Mapper
public interface PagedMapper {

	/**
	 * @param page
	 * @return {@link PagedRecommendationSummary}
	 */
	public PagedRecommendationSummary toPagedRecommendationSummary(PagedRecommendation page);
	
	/**
	 * @param page
	 * @return {@link PagedReviewSummary}
	 */
	public PagedReviewSummary toPageReviewSummary(PagedReview page);
}
