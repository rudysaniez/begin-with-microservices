package com.me.work.example.api.core.review;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.me.work.example.api.Api;

public interface ReviewService {

	/**
	 * @param id
	 * @return {@link Review}
	 */
	@GetMapping(value=Api.REVIEW_PATH + "/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
	public Review getReview(@PathVariable(name="id", required=true) String id);
	
	/**
	 * @param productId
	 * @return list of {@link Review}
	 */
	@GetMapping(value=Api.REVIEW_PATH, produces=MediaType.APPLICATION_JSON_VALUE)
	public List<Review> getReviewByProductId(@RequestParam(name="productId", required=true) String productId);
}
