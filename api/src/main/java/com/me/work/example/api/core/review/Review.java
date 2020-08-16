package com.me.work.example.api.core.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;

@AllArgsConstructor
@Data
public class Review {

	private final String reviewID;
	
	@Exclude
	private final String productID;
	
	@Exclude
	private final String author;
	
	@Exclude
	private final String subject;
	
	@Exclude
	private final String content;
	
	public Review() {
		
		this.reviewID = null;
		this.productID = null;
		this.author = null;
		this.subject = null;
		this.content = null;
	}
}
