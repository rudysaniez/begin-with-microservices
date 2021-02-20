package com.me.microservices.core.review.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import com.me.microservices.core.review.bo.ReviewEntity;
import com.me.microservices.core.review.repository.ReviewRepository;
import com.me.microservices.core.review.service.AsciiArtService;

@DataMongoTest
@RunWith(SpringRunner.class)
public class ReviewEntityTest {

	@Autowired ReviewRepository reviewRepository;
	@Autowired AsciiArtService artService;
	ReviewEntity savedReview;
	
	private static final Integer REVIEW_ID = 999;
	private static final Integer PRODUCT_ID = 999;
	private static final String SUBJECT = "Washing machine";
	private static final String CONTENT = "Good product. The installation is simply.";
	private static final String AUTHOR = "rudysaniez";
	
	private static final Pageable DEFAULT_PAGE = PageRequest.of(0, 5);
	
	@Before
	public void setupdb() {
		
		artService.display("SETUP");
		
		reviewRepository.deleteAll().block();
		
		ReviewEntity reviewEntity = new ReviewEntity(null, REVIEW_ID, PRODUCT_ID, AUTHOR, SUBJECT, CONTENT);
		savedReview = reviewRepository.save(reviewEntity).log().block();
		
		assertNotNull(savedReview.getId());
		assertEqualsReview(savedReview, reviewEntity);
	}
	
	@Test
	public void findByReviewID() {
		
		artService.display("FIND BY REVIEW ID");
		
		ReviewEntity entity = reviewRepository.findByReviewID(REVIEW_ID).log().block();
		assertEqualsReview(savedReview, entity);
	}
	
	@Test
	public void findByProductID() {
		
		List<ReviewEntity> reviews = reviewRepository.findByProductID(PRODUCT_ID, DEFAULT_PAGE).collectList().block();
		assertThat(reviews).isNotEmpty();
	}
	
	@Test
	public void createReview() {
		
		artService.display("CREATE REVIEW");
		
		ReviewEntity entity = new ReviewEntity(null, 1, 1, AUTHOR, SUBJECT, CONTENT);
		entity = reviewRepository.save(entity).log().block();
		
		assertNotNull(entity.getId());
		assertEquals(AUTHOR, entity.getAuthor());
		assertEquals(SUBJECT, entity.getSubject());
		assertEquals(CONTENT, entity.getContent());
	}
	
	@Test
	public void updateReview() {
		
		artService.display("UPDATE REVIEW");
		
		ReviewEntity entity = reviewRepository.findByReviewID(REVIEW_ID).log().block();
		assertEqualsReview(savedReview, entity);
		
		entity.setAuthor("rsaniez");
		entity = reviewRepository.save(entity).log().block();
		assertEquals("rsaniez", entity.getAuthor());
	}
	
	@Test
	public void deleteReview() {
		
		reviewRepository.delete(savedReview);
		reviewRepository.findByReviewID(REVIEW_ID);
	}
	
	@Test(expected=DuplicateKeyException.class)
	public void createReviewDataIntegrityViolationException() {
		
		ReviewEntity entity = new ReviewEntity(null, REVIEW_ID, PRODUCT_ID, AUTHOR, SUBJECT, CONTENT);
		reviewRepository.save(entity).block();
	}
	
	/**
	 * @param expectedReview
	 * @param actualReview
	 */
	public void assertEqualsReview(ReviewEntity expectedReview, ReviewEntity actualReview) {
		
		assertEquals(expectedReview.getContent(), actualReview.getContent());
		assertEquals(expectedReview.getAuthor(), actualReview.getAuthor());
		assertEquals(expectedReview.getProductID(), actualReview.getProductID());
		assertEquals(expectedReview.getReviewID(), actualReview.getReviewID());
		assertEquals(expectedReview.getSubject(), actualReview.getSubject());
	}
}
