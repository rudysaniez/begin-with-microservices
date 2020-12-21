package com.me.microservices.core.composite.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.me.microservices.core.productcomposite.api.model.ReviewSummary;
import com.me.microservices.core.review.api.model.Review;

@Mapper
public interface ReviewMapper {

	/**
	 * @param reviewSummary
	 * @return {@link Review}
	 */
	@Mappings(value= {
			@Mapping(target="productID", ignore=true), 
			@Mapping(target="creationDate", ignore=true),
			@Mapping(target="updateDate", ignore=true)})
	public Review toCoreModel(ReviewSummary reviewSummary);
	
	/**
	 * @param listOfReviewSummary
	 * @return list of {@link Review}
	 */
	public List<Review> toCoreModels(List<ReviewSummary> listOfReviewSummary);
	
	/**
	 * @param msReview
	 * @return {@link ReviewSummary}
	 */
	public ReviewSummary toSummary(Review msReview);
	
	/**
	 * @param listOfMsReview
	 * @return list of {@link ReviewSummary}
	 */
	public List<ReviewSummary> toSummaries(List<Review> listOfMsReview);
}
