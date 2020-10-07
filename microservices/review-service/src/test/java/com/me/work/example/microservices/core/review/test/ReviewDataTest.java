package com.me.work.example.microservices.core.review.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.me.work.example.handler.exception.NotFoundException;
import com.me.work.example.microservices.core.review.bo.ReviewEntity;
import com.me.work.example.microservices.core.review.repository.ReviewRepository;

@ActiveProfiles(profiles="testing")
@Transactional
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(connection=EmbeddedDatabaseConnection.HSQL)
@DataJpaTest
public class ReviewDataTest {

	@Autowired
	private ReviewRepository reviewRepository;
	
	private ReviewEntity savedReview;
	
	private static final Integer REVIEW_ID = 1;
	private static final Integer PRODUCT_ID = 1;
	private static final String SUBJECT = "Washing machine";
	private static final String CONTENT = "Good product. The installation is simply.";
	private static final String AUTHOR = "rudysaniez";
	
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
	private static final Pageable DEFAULT_PAGE = PageRequest.of(0, 5);
	
	@Before
	public void before() {
		
		ReviewEntity reviewEntity = new ReviewEntity();
		reviewEntity.setAuthor(AUTHOR);
		reviewEntity.setContent(CONTENT);
		reviewEntity.setProductID(PRODUCT_ID);
		reviewEntity.setReviewID(REVIEW_ID);
		reviewEntity.setSubject(SUBJECT);
		reviewEntity.setCreationDate(LocalDateTime.now());
		
		savedReview = reviewRepository.save(reviewEntity);
		
		assertNotNull(savedReview.getId());
		assertEqualsReview(savedReview, reviewEntity);
	}
	
	@Test
	public void getReviewByProductID() {
		
		Page<ReviewEntity> pageOfReview = reviewRepository.findByProductID(PRODUCT_ID, DEFAULT_PAGE);
		assertEqualsReview(savedReview, pageOfReview.get().findFirst().orElseThrow(() -> new NotFoundException()));
	}
	
	public void assertEqualsReview(ReviewEntity expectedReview, ReviewEntity actualReview) {
		
		assertEquals(expectedReview.getContent(), actualReview.getContent());
		assertEquals(expectedReview.getAuthor(), actualReview.getAuthor());
		assertEquals(expectedReview.getProductID(), actualReview.getProductID());
		assertEquals(expectedReview.getReviewID(), actualReview.getReviewID());
		assertEquals(expectedReview.getSubject(), actualReview.getSubject());
	}
	
	public void assertEqualsDate(LocalDateTime expected, LocalDateTime actual) {
		assertEquals(FORMATTER.format(expected), FORMATTER.format(actual));
	}
}