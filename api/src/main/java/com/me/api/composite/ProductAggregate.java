package com.me.api.composite;

import com.me.api.core.common.Paged;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;

@AllArgsConstructor @Data
public class ProductAggregate {

	private Integer productID;
	private String name;
	
	@Exclude
	private Integer weight;
	
	@Exclude
	private Paged<RecommendationSummary> recommendations;
	
	@Exclude
	private Paged<ReviewSummary> reviews;
	
	public ProductAggregate() {
		
		this.productID = 0;
		this.name = null;
		this.weight = 0;
		this.recommendations = null;
		this.reviews = null;
	}
}
