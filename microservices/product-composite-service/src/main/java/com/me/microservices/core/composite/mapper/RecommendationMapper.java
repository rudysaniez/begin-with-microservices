package com.me.microservices.core.composite.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.me.microservices.core.productcomposite.api.model.RecommendationSummary;
import com.me.microservices.core.recommendation.api.model.Recommendation;

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
	public Recommendation toCoreModel(RecommendationSummary recommendationSummary);
	
	/**
	 * @param listOfRecommendationSummary
	 * @return list of {@link Recommendation}
	 */
	public List<Recommendation> toCoreModels(List<RecommendationSummary> listOfRecommendationSummary);
	
	/**
	 * @param msModel
	 * @return {@link RecommendationSummary}
	 */
	public RecommendationSummary toSummary(Recommendation msRecommendation);
	
	/**
	 * @param listOfMsModel
	 * @return list of {@link RecommendationSummary}
	 */
	public List<RecommendationSummary> toSummaries(List<Recommendation> listOfMsRecommendation);
}
