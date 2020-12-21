package com.me.microservices.core.recommendation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.me.microservices.core.recommendation.api.model.Recommendation;
import com.me.microservices.core.recommendation.bo.RecommendationEntity;

@Mapper
public interface RecommendationMapper {

	/**
	 * @param recommendation
	 * @return {@link RecommendationEntity}
	 */
	@Mappings(value= {
			@Mapping(target="version", ignore=true),
			@Mapping(target="id", ignore=true)
	})
	public RecommendationEntity toEntity(Recommendation recommendation);
	
	/**
	 * @param recommendation
	 * @return {@link RecommendationEntity}
	 */
	public Recommendation toModel(RecommendationEntity recommendation);
}
