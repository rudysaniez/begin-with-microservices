package com.me.work.example.api.core.review;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.me.work.example.api.Api;
import com.me.work.example.api.core.common.Paged;

public interface ReviewService {

	/**
	 * @param reviewID
	 * @return {@link Review}
	 */
	@GetMapping(value=Api.REVIEW_PATH + "/{reviewID}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Review> getReview(@PathVariable(name="reviewID", required=true) Integer reviewID);
	
	/**
	 * @param productId
	 * @param pageNumber
	 * @param pageSize
	 * @return page of {@link Review}
	 */
	@GetMapping(value=Api.REVIEW_PATH, produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Paged<Review>> getReviewByProductId(@RequestParam(name="productId", required=true) Integer productID,
			@RequestParam(name="pageNumber", required=false) Integer pageNumber, 
				@RequestParam(name="pageSize", required=false) Integer pageSize);
	
	/**
	 * @param review
	 * @return {@link Review}
	 */
	@PostMapping(value=Api.REVIEW_PATH, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Review> save(@RequestBody Review review);
	
	/**
	 * @param review
	 * @param reviewID
	 * @return {@link Review}
	 */
	@PutMapping(value=Api.REVIEW_PATH + "/{reviewID}", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Review> update(@RequestBody Review review, @PathVariable(name="reviewID", required=true) Integer reviewID);
	
	/**
	 * @param reviewID
	 */
	@DeleteMapping(value=Api.REVIEW_PATH + "/{reviewID}")
	public void deleteReview(@PathVariable(name="reviewID", required=true) Integer reviewID);
}
