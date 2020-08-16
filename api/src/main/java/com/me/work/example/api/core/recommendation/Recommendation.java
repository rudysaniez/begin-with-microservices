package com.me.work.example.api.core.recommendation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;

@AllArgsConstructor
@Data
public class Recommendation {

	private final String recommendationID;
	
	@Exclude
	private final String productID;
	
	@Exclude
	private final String author;
	
	@Exclude
	private final Integer rate;
	
	@Exclude
	private final String content;
	
	public Recommendation() {
		
		this.recommendationID = null;
		this.productID = null;
		this.author = null;
		this.rate = null;
		this.content = null;
	}
}
