package com.me.api.composite;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;

@Data @AllArgsConstructor
public class RecommendationSummary {

	private Integer recommendationID;
	
	@Exclude 
	private String author;
	
	@Exclude 
	private Integer rate;
	
	@Exclude
	private String content;
	
	public RecommendationSummary() {
		
		this.recommendationID = 0;
		this.author = null;
		this.rate = 0;
		this.content = null;
	}
}
