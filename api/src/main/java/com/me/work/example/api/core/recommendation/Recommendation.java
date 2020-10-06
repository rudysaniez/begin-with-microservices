package com.me.work.example.api.core.recommendation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor @Data
public class Recommendation {

	private Integer recommendationID;
	
	@Exclude
	private Integer productID;
	
	@Exclude
	private String author;
	
	@Exclude
	private Integer rate;
	
	@Exclude
	private String content;
}
