package com.me.work.example.microservices.core.review.mapper;

import org.mapstruct.Mapper;

import com.me.work.example.api.core.review.Review;
import com.me.work.example.microservices.core.review.bo.ReviewEntity;

@Mapper
public interface ReviewMapper {

	/**
	 * @param review
	 * @return {@link ReviewEntity}
	 */
	public ReviewEntity toEntity(Review review);
	
	/**
	 * @param review
	 * @return {@link Review}
	 */
	public Review toModel(ReviewEntity review);
}
