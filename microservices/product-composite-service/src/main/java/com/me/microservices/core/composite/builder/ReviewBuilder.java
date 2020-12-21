package com.me.microservices.core.composite.builder;

import java.time.LocalDateTime;

import com.me.microservices.core.review.api.model.Review;

public class ReviewBuilder {

	private Integer reviewID;
	private Integer productID;
	private String author;
	private String subject;
	private String content;
	private LocalDateTime creationDate;
	
	private ReviewBuilder() {}
	
	public static ReviewBuilder create() {
		return new ReviewBuilder();
	}
	
	public ReviewBuilder withReviewID(Integer reviewID) {
		this.reviewID = reviewID;
		return this;
	}
	
	public ReviewBuilder withProductID(Integer productID) {
		this.productID = productID;
		return this;
	}
	
	public ReviewBuilder withAuthor(String author) {
		this.author = author;
		return this;
	}
	
	public ReviewBuilder withSubject(String subject) {
		this.subject = subject;
		return this;
	}
	
	public ReviewBuilder withContent(String content) {
		this.content = content;
		return this;
	}
	
	public ReviewBuilder withCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
		return this;
	}
	
	public Review build() {
		
		Review review = new Review();
		review.setAuthor(author);
		review.setContent(content);
		review.setProductID(productID);
		review.setReviewID(reviewID);
		review.setSubject(subject);
		review.setCreationDate(creationDate);
		return review;
	}
}
