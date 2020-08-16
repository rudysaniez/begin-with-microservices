package com.me.work.example.microservices.core.recommendation.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import com.me.work.example.api.core.recommendation.Recommendation;
import com.me.work.example.api.core.recommendation.RecommendationService;
import com.me.work.example.handler.exception.InvalidInputException;
import com.me.work.example.handler.exception.NotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class RecommendationServiceImpl implements RecommendationService {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Recommendation getRecommendation(String id) {
		
		if(id.equals("13")) throw new NotFoundException(String.format("The recommendation %s doesn't not exit", id));
		if(id.equals("15")) throw new InvalidInputException(String.format("The recommendation %s is an invalid input", id));
		
		if(log.isInfoEnabled())
			log.info("The recommendation {} found", id);
		
		return new Recommendation(id, id, "rudysaniez", 1, "VALIDATED");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Recommendation> getRecommendationByProductId(String productId) {
		
		if(productId.equals("13")) throw new NotFoundException(String.format("The recommendation %s doesn't not exit", productId));
		if(productId.equals("15")) throw new InvalidInputException(String.format("The recommendation %s is an invalid input", productId));
		
		List<Recommendation> recommendations = new ArrayList<>();
		recommendations.add(this.buildRecommendation("1", productId));
		recommendations.add(this.buildRecommendation("2", productId));
		recommendations.add(this.buildRecommendation("3", productId));
		
		if(log.isInfoEnabled())
			log.info("{} recommendations found with productId={}", recommendations.size(), productId);
		
		return recommendations;
	}
	
	/**
	 * @param id
	 * @param productId
	 * @return {@link Recommendation}
	 */
	private Recommendation buildRecommendation(String id, String productId) {
		return new Recommendation(id, productId, "rudysaniez", 1, "VALIDATED");
	}
}
