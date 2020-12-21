package com.me.microservices.core.review.consumer;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.me.api.event.Event;
import com.me.microservices.core.review.api.ReviewsApi;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ReviewConsumer {

	private final ReviewsApi reviewService;
	
	@Autowired
	public ReviewConsumer(ReviewsApi reviewService) {
		this.reviewService = reviewService;
	}
	
	/**
	 * @param event
	 */
	@StreamListener(value = Sink.INPUT)
	public void consume(@Payload(required = false) Event<Integer> event) {
		
		if(event == null)
			return;
		
		log.info(" > Receive an event of type {}, created at {}", event.getType(), event.getCreationDate());
		
		switch(event.getType()) {
		
		case DELETE:
			reviewService.deleteReviews(event.getKey(), null).block();
			log.info(" > The review(s) with productID={} has been deleted at {}", event.getKey(), LocalDateTime.now());
			break;
		}
	}
}
