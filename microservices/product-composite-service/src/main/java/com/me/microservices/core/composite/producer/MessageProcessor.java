package com.me.microservices.core.composite.producer;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface MessageProcessor {

	public static final String OUTPUT_PRODUCTS = "output-products";
	public static final String OUTPUT_RECOMMENDATIONS = "output-recommendations";
	public static final String OUTPUT_REVIEWS = "output-reviews";
	
	@Output(OUTPUT_PRODUCTS)
	public MessageChannel outputProducts();
	
	@Output(OUTPUT_RECOMMENDATIONS)
	public MessageChannel outputRecommendations();
	
	@Output(OUTPUT_REVIEWS)
	public MessageChannel outputReviews();
}
