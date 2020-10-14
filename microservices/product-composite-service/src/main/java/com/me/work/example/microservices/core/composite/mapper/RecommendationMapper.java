package com.me.work.example.microservices.core.composite.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.me.work.example.api.core.composite.RecommendationSummary;
import com.me.work.example.api.core.recommendation.Recommendation;

@Mapper
public interface RecommendationMapper {

	@Mappings(value= {
			@Mapping(target="productID", ignore=true), 
			@Mapping(target="creationDate", ignore=true),
			@Mapping(target="updateDate", ignore=true)})
	public Recommendation toModel(RecommendationSummary recommendationSummary);
}
