package com.me.microservices.core.composite.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.me.api.core.composite.RecommendationSummary;
import com.me.api.core.recommendation.Recommendation;

@Mapper
public interface RecommendationMapper {

	@Mappings(value= {
			@Mapping(target="productID", ignore=true), 
			@Mapping(target="creationDate", ignore=true),
			@Mapping(target="updateDate", ignore=true)})
	public Recommendation toModel(RecommendationSummary recommendationSummary);
}