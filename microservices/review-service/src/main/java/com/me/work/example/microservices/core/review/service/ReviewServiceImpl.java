package com.me.work.example.microservices.core.review.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import com.me.work.example.api.core.review.Review;
import com.me.work.example.api.core.review.ReviewService;
import com.me.work.example.handler.exception.InvalidInputException;
import com.me.work.example.handler.exception.NotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ReviewServiceImpl implements ReviewService {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Review getReview(String id) {
		
		if(id.equals("13")) throw new NotFoundException(String.format("The review %s doesn't not exist", id));
		if(id.equals("15")) throw new InvalidInputException(String.format("The review %s is an invalid input", id));
		
		if(log.isDebugEnabled())
			log.debug("The review {} found", id);
		
		return new Review(id, id, "rudysaniez", "review 1", "VALIDATED");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Review> getReviewByProductId(String productId) {
		
		if(productId.equals("13")) throw new NotFoundException(String.format("The product %s doesn't not exist", productId));
		if(productId.equals("15")) throw new InvalidInputException(String.format("The product %s is an invalid input", productId));
		
		
		List<Review> reviews = new ArrayList<>();
		reviews.add(this.buildReview("1", productId));
		reviews.add(this.buildReview("2", productId));
		reviews.add(this.buildReview("3", productId));
		
		if(log.isDebugEnabled())
			log.debug("{} reviews found with productId={}", reviews.size(), productId);
		
		return reviews;
	}
	
	/**
	 * @param reviewId
	 * @param productId
	 * @return
	 */
	private Review buildReview(String reviewId, String productId) {
		return new Review(reviewId, productId, "rudysaniez", "review-"+reviewId, "VALIDATED");
	}
}
