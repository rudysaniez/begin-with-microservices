package com.me.microservices.core.composite.builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.me.microservices.core.recommendation.api.model.PageMetadata;
import com.me.microservices.core.recommendation.api.model.PagedRecommendation;
import com.me.microservices.core.recommendation.api.model.Recommendation;

public class PagedRecommendationBuilder {

	private List<Recommendation> recommendations = new ArrayList<>();
	private PageMetadata page = new PageMetadata();
	
	private PagedRecommendationBuilder() {}
	
	public static PagedRecommendationBuilder create() {
		return new PagedRecommendationBuilder();
	}
	
	public PagedRecommendationBuilder withRecommendation(Integer recommendationID, Integer productID, String author, 
			Integer rate, String content, LocalDateTime creationDate) {
		
		recommendations.add(RecommendationBuilder.create().withAuthor(author).withContent(content).
				withCreationDate(creationDate).withProductID(productID).
				withRecommendationID(recommendationID).build());
		
		return this;
	}
	
	public PagedRecommendationBuilder withPageMetadata(Long size, Long totalElements, Long totalPages, Long number) {
		
		page.setSize(size);
		page.setTotalElements(totalElements);
		page.setTotalPages(totalPages);
		page.setNumber(number);
		return this;
	}
	
	public PagedRecommendation build() {
		
		PagedRecommendation pr = new PagedRecommendation();
		pr.setContent(recommendations);
		pr.setPage(page);
		return pr;
	}
}
