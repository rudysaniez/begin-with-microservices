package com.me.api.core.review;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;
import lombok.NoArgsConstructor;

@NoArgsConstructor @Data
public class Review  {

	private Integer reviewID;
	
	@Exclude
	private Integer productID;
	
	@Exclude
	private String author;
	
	@Exclude
	private String subject;
	
	@Exclude
	private String content;
	
	@Exclude
	private LocalDateTime creationDate;
	
	@Exclude
	private LocalDateTime updateDate;
	
	/**
	 * @param reviewID
	 * @param productID
	 * @param author
	 * @param subject
	 * @param content
	 */
	public Review(Integer reviewID, Integer productID, String author, String subject, String content) {
		
		this.reviewID = reviewID;
		this.productID = productID;
		this.author = author;
		this.subject = subject;
		this.content = content;
		this.creationDate = LocalDateTime.now();
		this.updateDate = null;
	}
}
