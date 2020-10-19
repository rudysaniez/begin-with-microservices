package com.me.api.core.recommendation;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;
import lombok.NoArgsConstructor;

@NoArgsConstructor @Data
public class Recommendation {

	private Integer recommendationID;
	
	private Integer productID;
	
	@Exclude
	private String author;
	
	@Exclude
	private Integer rate;
	
	@Exclude
	private String content;
	
	@Exclude
	private LocalDateTime creationDate;
	
	@Exclude
	private LocalDateTime updateDate;
	
	/**
	 * @param recommendationID
	 * @param productID
	 * @param author
	 * @param rate
	 * @param content
	 */
	public Recommendation(Integer recommendationID, Integer productID, String author, Integer rate, String content) {
		
		this.recommendationID = recommendationID;
		this.productID = productID;
		this.author = author;
		this.rate = rate;
		this.content = content;
		this.creationDate = LocalDateTime.now();
		this.updateDate = null;
	}
 }
