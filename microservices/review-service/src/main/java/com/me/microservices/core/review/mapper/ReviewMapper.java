package com.me.microservices.core.review.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.me.microservices.core.review.api.model.Review;
import com.me.microservices.core.review.bo.ReviewEntity;

@Mapper
public interface ReviewMapper {

	/**
	 * @param review
	 * @return {@link ReviewEntity}
	 */
	@Mappings(value = {@Mapping(target = "id", ignore = true), 
			@Mapping(target = "withId", ignore = true), 
			@Mapping(target = "version", ignore = true)})
	@Mapping(target = "id", ignore = true)
	public ReviewEntity toEntity(Review review);
	
	/**
	 * @param review
	 * @return {@link Review}
	 */
	public Review toModel(ReviewEntity review);
	
	/**
	 * @param reviews
	 * @return list of {@link Review}
	 */
	public List<Review> toListModel(List<ReviewEntity> reviews);
}
