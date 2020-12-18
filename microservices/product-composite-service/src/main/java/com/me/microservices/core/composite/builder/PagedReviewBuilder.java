package com.me.microservices.core.composite.builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.me.microservices.core.review.api.model.PageMetadata;
import com.me.microservices.core.review.api.model.PagedReview;
import com.me.microservices.core.review.api.model.Review;

public class PagedReviewBuilder {

	private List<Review> reviews = new ArrayList<>();
	private PageMetadata page = new PageMetadata();
	
	private PagedReviewBuilder() {}
	
	public static PagedReviewBuilder create() {
		return new PagedReviewBuilder();
	}
	
	public PagedReviewBuilder withReview(Integer reviewID, Integer productID, String author, 
			String subject, String content, LocalDateTime creationDate) {
		
		reviews.add(ReviewBuilder.create().withAuthor(author).withContent(content).withCreationDate(creationDate).
			withProductID(productID).withReviewID(reviewID).withSubject(subject).build());
		
		return this;
	}
	
	public PagedReviewBuilder withPageMetadata(Long size, Long totalElements, Long totalPages, Long number) {
		
		page.setSize(size);
		page.setTotalElements(totalElements);
		page.setTotalPages(totalPages);
		page.setNumber(number);
		return this;
	}
	
	public PagedReview build() {
		
		PagedReview pr = new PagedReview();
		pr.setContent(reviews);
		pr.setPage(page);
		return pr;
	}
	
}
