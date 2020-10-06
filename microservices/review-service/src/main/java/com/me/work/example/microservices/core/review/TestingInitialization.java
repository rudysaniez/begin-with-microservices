package com.me.work.example.microservices.core.review;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.me.work.example.microservices.core.review.bo.ReviewEntity;
import com.me.work.example.microservices.core.review.repository.ReviewRepository;

import lombok.extern.slf4j.Slf4j;

@Profile("testing")
@Slf4j
@Transactional
@Component
public class TestingInitialization implements ApplicationRunner {

	private final ReviewRepository reviewRepository;
	
	public static final Integer DEFAULT_REVIEW_ID = 999;
	public static final Integer DEFAULT_PRODUCT_ID = 999;
	
	@Autowired
	public TestingInitialization(ReviewRepository reviewRepository) {
		this.reviewRepository = reviewRepository;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(ApplicationArguments args) throws Exception {
		
		Optional<ReviewEntity> optionalOfReview = reviewRepository.findByReviewID(DEFAULT_REVIEW_ID);
		
		if(optionalOfReview.isEmpty()) {
			
			ReviewEntity review = new ReviewEntity(null, DEFAULT_REVIEW_ID, DEFAULT_PRODUCT_ID, 
					"rudysaniez", "Rate product", "Product is very good quality.", LocalDateTime.now(), null);
			review = reviewRepository.save(review);
			
			if(log.isInfoEnabled()) 
				log.info(" > Review with id={} has been created. ({})", review.getId(), review.toString());
		}
		else if(log.isInfoEnabled()) 
			log.info(" > Review with reviewID={} already exists.", optionalOfReview.get().getReviewID());
	}
}
