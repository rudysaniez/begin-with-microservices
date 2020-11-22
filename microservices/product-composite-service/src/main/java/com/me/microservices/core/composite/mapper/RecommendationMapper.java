package com.me.microservices.core.composite.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.me.api.core.composite.RecommendationSummary;
import com.me.api.core.recommendation.Recommendation;

@Mapper
public interface RecommendationMapper {

	/**
	 * @param recommendationSummary
	 * @return {@link Recommendation}
	 */
	@Mappings(value= {
			@Mapping(target="productID", ignore=true), 
			@Mapping(target="creationDate", ignore=true),
			@Mapping(target="updateDate", ignore=true)})
	public Recommendation toMsModel(RecommendationSummary recommendationSummary);
	
	/**
	 * @param listOfRecommendationSummary
	 * @return list of {@link Recommendation}
	 */
	public List<Recommendation> toListOfMsModel(List<RecommendationSummary> listOfRecommendationSummary);
	
	/**
	 * @param msModel
	 * @return {@link RecommendationSummary}
	 */
	public RecommendationSummary toModel(Recommendation msRecommendation);
	
	/**
	 * @param listOfMsModel
	 * @return list of {@link RecommendationSummary}
	 */
	public List<RecommendationSummary> toListOfModel(List<Recommendation> listOfMsRecommendation);
}
