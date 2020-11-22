package com.me.microservices.core.recommendation.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

import com.me.microservices.core.recommendation.bo.RecommendationEntity;
import com.me.microservices.core.recommendation.repository.RecommendationRepository;

import reactor.test.StepVerifier;

@RunWith(SpringRunner.class)
@DataMongoTest
public class RecommendationEntityTest {

	@Autowired
	private RecommendationRepository recommendationRepository;
	
	private RecommendationEntity savedRecommendation;
	
	private static final Integer RECOMMENDATION_ID = 1;
	private static final Integer PRODUCT_ID = 1;
	private static final String AUTHOR = "rudysaniez";
	private static final String CONTENT = "This product is very good !";
	private static final Integer RATE = 1;
	
	@Before
	public void setupdb() {
	
		recommendationRepository.deleteAll().block();
		
		StepVerifier.create(recommendationRepository.save(new RecommendationEntity(RECOMMENDATION_ID, PRODUCT_ID, 
				AUTHOR, RATE, CONTENT))).
		expectNextMatches(entity -> entity.getRecommendationID().equals(RECOMMENDATION_ID)).verifyComplete();
		
		savedRecommendation = recommendationRepository.findByRecommendationID(RECOMMENDATION_ID).block();
	}
	
	@Test
	public void create() {
		
		StepVerifier.create(recommendationRepository.findByRecommendationID(RECOMMENDATION_ID)).
			expectNextMatches(entity -> entity.getRecommendationID().equals(RECOMMENDATION_ID)).verifyComplete();
		
		StepVerifier.create(recommendationRepository.save(new RecommendationEntity(2, 2, AUTHOR, RATE, CONTENT))).
			expectNextMatches(entity -> entity.getRecommendationID().equals(2)).verifyComplete();
		
		StepVerifier.create(recommendationRepository.findByRecommendationID(2)).
			expectNextMatches(entity -> entity.getRecommendationID().equals(2)).verifyComplete();
	}
	
	@Test
	public void update() {
		
		RecommendationEntity entity = recommendationRepository.findByRecommendationID(RECOMMENDATION_ID).block();
		
		entity.setAuthor("rsaniez");
		entity.setRate(3);
		
		StepVerifier.create(recommendationRepository.save(entity)).
			expectNextMatches(e -> e.getAuthor().equals("rsaniez") && e.getRate().intValue() == 3).
			verifyComplete();
	}
	
	@Test
	public void delete() {
		
		recommendationRepository.delete(savedRecommendation).block();
		StepVerifier.create(recommendationRepository.findByRecommendationID(RECOMMENDATION_ID)).verifyComplete();
	}
	
	@Test
	public void findByRecommendationByID() {
		
		StepVerifier.create(recommendationRepository.findByRecommendationID(RECOMMENDATION_ID)).
			expectNextMatches(entity -> entity.getContent().equals(CONTENT)).verifyComplete();
	}
	
	@Test
	public void findByProductID() {
		
		List<RecommendationEntity> listOfRecommendations = recommendationRepository.findByProductID(PRODUCT_ID, 
				PageRequest.of(0, 10, Sort.by(Direction.ASC, "recommendationID"))).collectList().block();
		
		assertThat(listOfRecommendations).hasSize(1);
		assertEquals(savedRecommendation, listOfRecommendations.get(0));
	}
	
	@Test
	public void recommendationNotFound() {
		
		recommendationRepository.delete(savedRecommendation).block();
		StepVerifier.create(recommendationRepository.findByRecommendationID(RECOMMENDATION_ID)).verifyComplete();
	}
	
	@Test
	public void duplicateError() {
		
		StepVerifier.create(recommendationRepository.findByRecommendationID(RECOMMENDATION_ID)).
			expectNextMatches(entity -> entity.getContent().equals(CONTENT)).verifyComplete();
		
		RecommendationEntity entity = new RecommendationEntity(RECOMMENDATION_ID, 1, AUTHOR, RATE, CONTENT);
		StepVerifier.create(recommendationRepository.save(entity)).expectError(DuplicateKeyException.class).verify();
	}
	
	@Test
	public void optimisticLockError() {
		
		RecommendationEntity entity1 = recommendationRepository.findByRecommendationID(RECOMMENDATION_ID).block();
		RecommendationEntity entity2 = recommendationRepository.findByRecommendationID(RECOMMENDATION_ID).block();
		
		entity1.setRate(3);
		recommendationRepository.save(entity1).block();
		
		entity2.setRate(2);
		StepVerifier.create(recommendationRepository.save(entity2)).
			expectError(OptimisticLockingFailureException.class).verify();
	}
}
