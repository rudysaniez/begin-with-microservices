package com.me.api.composite;

import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;

@Deprecated
@Data @AllArgsConstructor
public class ProductComposite {

	private Integer productID;
	private String name;
	
	@Exclude
	private Integer weight;
	
	@Exclude
	private List<RecommendationSummary> recommendations;
	
	@Exclude
	private List<ReviewSummary> reviews;
	
	@SuppressWarnings("unchecked")
	public ProductComposite() {
		
		this.productID = 0;
		this.name = null;
		this.weight = null;
		this.recommendations = Collections.EMPTY_LIST;
		this.reviews = Collections.EMPTY_LIST;
	}
}
