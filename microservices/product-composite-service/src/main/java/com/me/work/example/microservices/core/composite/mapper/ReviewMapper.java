package com.me.work.example.microservices.core.composite.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.me.work.example.api.core.composite.ReviewSummary;
import com.me.work.example.api.core.review.Review;

@Mapper
public interface ReviewMapper {

	@Mappings(value= {
			@Mapping(target="productID", ignore=true), 
			@Mapping(target="creationDate", ignore=true),
			@Mapping(target="updateDate", ignore=true)})
	public Review toModel(ReviewSummary reviewSummary);
}
