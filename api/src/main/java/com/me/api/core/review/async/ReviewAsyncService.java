package com.me.api.core.review.async;

public interface ReviewAsyncService {

	/**
	 * @param productID
	 */
	public void deleteReviewsAsync(Integer productID);
}
