package com.me.microservices.core.composite.mapper;

import org.mapstruct.Mapper;

import com.me.api.core.common.Paged;
import com.me.api.core.composite.RecommendationSummary;
import com.me.api.core.composite.ReviewSummary;
import com.me.api.core.recommendation.Recommendation;
import com.me.api.core.review.Review;

@Mapper(uses= {RecommendationMapper.class, ReviewMapper.class})
public interface PagedMapper {

	/**
	 * @param msPaged
	 * @return page of {@link RecommendationSummary}
	 */
	public Paged<RecommendationSummary> toPageRecommendationModel(Paged<Recommendation> msPaged);
	
	/**
	 * @param msPaged
	 * @return page of {@link ReviewSummary}
	 */
	public Paged<ReviewSummary> toPageReviewModel(Paged<Review> msPaged);
	
	
	/**
	 * @param pageModel
	 * @return page of {@link Recommendation}
	 */
	public Paged<Recommendation> toMsPageRecommendation(Paged<RecommendationSummary> pageModel);
	
	/**
	 * @param pageModel
	 * @return page of {@link Review}
	 */
	public Paged<Review> toMsPageReview(Paged<ReviewSummary> pageModel);
}
