package com.me.microservices.core.review.test;

import static org.junit.Assert.assertNotNull;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.me.handler.exception.NotFoundException;
import com.me.microservices.core.review.bo.ReviewEntity;
import com.me.microservices.core.review.repository.ReviewRepository;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.test.StepVerifier;

@DataJpaTest
@AutoConfigureTestDatabase(connection=EmbeddedDatabaseConnection.HSQL)
@Transactional
@RunWith(SpringRunner.class)
public class MonoReviewEntityTest {

	@Autowired
	private Scheduler scheduler;
	
	@Autowired
	private ReviewRepository reviewRepository;
	
	private ReviewEntity savedReview;
	
	private static final Integer REVIEW_ID = 999;
	private static final Integer PRODUCT_ID = 999;
	private static final String SUBJECT = "Washing machine";
	private static final String CONTENT = "Good product. The installation is simply.";
	private static final String AUTHOR = "rudysaniez";
	
	@Before
	public void setupdb() {
		
		reviewRepository.deleteAll();
		
		ReviewEntity reviewEntity = new ReviewEntity();
		reviewEntity.setAuthor(AUTHOR);
		reviewEntity.setContent(CONTENT);
		reviewEntity.setProductID(PRODUCT_ID);
		reviewEntity.setReviewID(REVIEW_ID);
		reviewEntity.setSubject(SUBJECT);
		reviewEntity.setCreationDate(LocalDateTime.now());
		
		savedReview = reviewRepository.save(reviewEntity);
		
		assertNotNull(savedReview.getId());
	}
	
	@Test
	public void getReview() {
		
		Mono<ReviewEntity> monoOfReview = Mono.just(reviewRepository.findByReviewID(REVIEW_ID)).
				switchIfEmpty(Mono.error(new NotFoundException())).
				log();
		
		StepVerifier.create(Mono.defer(() -> monoOfReview).subscribeOn(scheduler)).
			expectNextMatches(entity -> entity.getContent().equals(CONTENT)).verifyComplete();
	}
}
