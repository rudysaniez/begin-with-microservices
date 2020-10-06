package com.me.work.example.microservices.core.recommendation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.me.work.example.api.core.recommendation.Recommendation;
import com.me.work.example.microservices.core.recommendation.bo.RecommendationEntity;

@Mapper
public interface RecommendationMapper {

	/**
	 * @param recommendation
	 * @return {@link RecommendationEntity}
	 */
	public RecommendationEntity toBusinessObject(Recommendation recommendation);
	
	/**
	 * @param recommendation
	 * @return {@link RecommendationEntity}
	 */
	@Mappings(value= {
			@Mapping(target="version", ignore=true),
			@Mapping(target="id", ignore=true)
	})
	public Recommendation toModel(RecommendationEntity recommendation);
}
