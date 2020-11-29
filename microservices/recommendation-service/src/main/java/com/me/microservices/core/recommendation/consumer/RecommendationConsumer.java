package com.me.microservices.core.recommendation.consumer;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.me.api.core.recommendation.Recommendation;
import com.me.api.core.recommendation.RecommendationService;
import com.me.api.event.Event;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RecommendationConsumer {

	private final RecommendationService recommendationService;
	
	@Autowired
	public RecommendationConsumer(RecommendationService recommendationService) {
		this.recommendationService = recommendationService;
	}
	
	/**
	 * @param event
	 */
	@StreamListener(value = Sink.INPUT)
	public void consumer(@Payload(required = false) Event<Integer, Recommendation> event) {
		
		if(event == null)
			return;
		
		log.info(" > Receive an event of type {}, created at {}", event.getType(), event.getCreationDate());
		
		switch(event.getType()) {
		
		case DELETE:
			recommendationService.deleteRecommendations(event.getKey()).block();
			log.info(" > The recommendation(s) with productID={} has been deleted at {}", event.getKey(), LocalDateTime.now());
			break;
		}
	}
}
