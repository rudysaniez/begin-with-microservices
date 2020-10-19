package com.me.microservices.core.recommendation.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

import com.me.handler.exception.NotFoundException;
import com.me.microservices.core.recommendation.bo.RecommendationEntity;
import com.me.microservices.core.recommendation.repository.RecommendationRepository;

@RunWith(SpringRunner.class)
@DataMongoTest
public class RecommendationEntityTest {

	@Autowired
	private RecommendationRepository recommendationRepository;
	
	private RecommendationEntity savedRecommendation;
	
	private static final Integer RECOMMENDATION_ID = 999;
	private static final Integer PRODUCT_ID = 999;
	private static final String AUTHOR = "rudysaniez";
	private static final String CONTENT = "This product is very good !";
	private static final Integer RATE = 1;
	
	@Before
	public void setupdb() {
	
		recommendationRepository.deleteAll();
		RecommendationEntity entity = new RecommendationEntity(RECOMMENDATION_ID, PRODUCT_ID, AUTHOR, RATE, CONTENT);
		savedRecommendation = recommendationRepository.save(entity);
		assertNotNull(savedRecommendation.getId());
		assertEqualsRecommendation(entity, savedRecommendation);
	}
	
	@Test
	public void creation() {
		
		RecommendationEntity entity = new RecommendationEntity(1, 1, AUTHOR, RATE, CONTENT);
		entity = recommendationRepository.save(entity);
		
		RecommendationEntity foundEntity = recommendationRepository.findByRecommendationID(1).
				orElseThrow(() -> new NotFoundException());
		
		assertEqualsRecommendation(entity, foundEntity);
	}
	
	@Test
	public void update() {
		
		RecommendationEntity entity = recommendationRepository.findByRecommendationID(RECOMMENDATION_ID).
				orElseThrow(() -> new NotFoundException());
		
		assertEqualsRecommendation(savedRecommendation, entity);
		
		entity.setAuthor("rsaniez");
		savedRecommendation = recommendationRepository.save(entity);
		assertEqualsRecommendation(savedRecommendation, entity);
		
		assertEquals(entity.getAuthor(), "rsaniez");
	}
	
	@Test(expected=NotFoundException.class)
	public void delete() {
		
		recommendationRepository.delete(savedRecommendation);
		recommendationRepository.findByRecommendationID(RECOMMENDATION_ID).orElseThrow(() -> new NotFoundException());
	}
	
	@Test(expected=DuplicateKeyException.class)
	public void duplicateError() {
		
		RecommendationEntity entity = new RecommendationEntity(RECOMMENDATION_ID, 1, AUTHOR, RATE, CONTENT);
		recommendationRepository.save(entity);
	}
	
	@Test(expected=OptimisticLockingFailureException.class)
	public void optimisticLockError() {
		
		RecommendationEntity entity1 = recommendationRepository.findByRecommendationID(RECOMMENDATION_ID).get();
		RecommendationEntity entity2 = recommendationRepository.findByRecommendationID(RECOMMENDATION_ID).get();
		
		entity1.setRate(3);
		recommendationRepository.save(entity1);
		
		entity2.setRate(2);
		recommendationRepository.save(entity2);
	}
	
	@Test
	public void getRecommendationByID() {
		
		RecommendationEntity entity = recommendationRepository.findByRecommendationID(RECOMMENDATION_ID).
				orElseThrow(() -> new NotFoundException());
		
		assertEqualsRecommendation(savedRecommendation, entity);
	}
	
	@Test
	public void getRecommendationByProductID() {
		
		recommendationRepository.deleteAll();
		
		recommendationRepository.saveAll(
			IntStream.rangeClosed(1000, 1010).mapToObj(i -> new RecommendationEntity(i, i, AUTHOR, RATE, CONTENT)).
			collect(Collectors.toList())
		);
		
		Pageable page = PageRequest.of(0, 3, Direction.ASC, "recommendationID");
		
		page = assertEqualsNextPage(page, "[1000, 1001, 1002]", true);
		page = assertEqualsNextPage(page, "[1003, 1004, 1005]", true);
		page = assertEqualsNextPage(page, "[1006, 1007, 1008]", true);
		page = assertEqualsNextPage(page, "[1009, 1010]", false);
	}
	
	public Pageable assertEqualsNextPage(Pageable page, String IDs, boolean expectNextPage) {
		
		Page<RecommendationEntity> pageOfRecommendation = recommendationRepository.findAll(page);
		
		assertEquals(pageOfRecommendation.stream().map(r -> r.getRecommendationID()).collect(Collectors.toList()).toString(), IDs);
		assertEquals(expectNextPage, pageOfRecommendation.hasNext());
		
		return page.next();
	}
	
	public void assertEqualsRecommendation(RecommendationEntity expectEntity, RecommendationEntity actualEntity) {
		
		assertEquals(expectEntity.getAuthor(), actualEntity.getAuthor());
		assertEquals(expectEntity.getContent(), actualEntity.getContent());
		assertEquals(expectEntity.getRate(), actualEntity.getRate());
		assertEquals(expectEntity.getRecommendationID(), actualEntity.getRecommendationID());
		assertEquals(expectEntity.getProductID(), actualEntity.getProductID());
	}
}
