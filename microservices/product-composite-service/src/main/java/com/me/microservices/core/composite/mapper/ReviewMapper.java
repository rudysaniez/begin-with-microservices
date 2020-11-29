package com.me.microservices.core.composite.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.me.api.composite.ReviewSummary;
import com.me.api.core.review.Review;

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
	public Review toMsModel(ReviewSummary reviewSummary);
	
	/**
	 * @param listOfReviewSummary
	 * @return list of {@link Review}
	 */
	public List<Review> toListOfMsModel(List<ReviewSummary> listOfReviewSummary);
	
	/**
	 * @param msReview
	 * @return {@link ReviewSummary}
	 */
	public ReviewSummary toModel(Review msReview);
	
	/**
	 * @param listOfMsReview
	 * @return list of {@link ReviewSummary}
	 */
	public List<ReviewSummary> toListOfModel(List<Review> listOfMsReview);
}
