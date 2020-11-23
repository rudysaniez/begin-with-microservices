package com.me.microservices.core.review.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.me.api.core.review.Review;
import com.me.microservices.core.review.bo.ReviewEntity;

@Mapper
public interface ReviewMapper {

	/**
	 * @param review
	 * @return {@link ReviewEntity}
	 */
	@Mapping(target = "id", ignore = true)
	public ReviewEntity toEntity(Review review);
	
	/**
	 * @param review
	 * @return {@link Review}
	 */
	public Review toModel(ReviewEntity review);
}
