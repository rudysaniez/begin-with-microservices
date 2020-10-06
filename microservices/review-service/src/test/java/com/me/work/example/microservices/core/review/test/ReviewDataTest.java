package com.me.work.example.microservices.core.review.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.me.work.example.microservices.core.review.TestingInitialization;
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
	
	private static final String SUBJECT = "Washing machine";
	private static final String CONTENT = "Good product. The installation is simply.";
	
	private static Integer ID = null;
	
	@Before
	public void before() {
		
		ReviewEntity re = new ReviewEntity();
		re.setAuthor("rudysaniez");
		re.setContent(CONTENT);
		re.setProductID(1);
		re.setReviewID(1);
		re.setSubject(SUBJECT);
		re.setCreationDate(LocalDateTime.now());
		
		re = reviewRepository.save(re);
		
		assertThat(re.getId()).isNotNull();
		
		ID = re.getId();
	}
	
	@Test
	public void test() {
		
		Page<ReviewEntity> pageOfReview = reviewRepository.findByProductID(1, PageRequest.of(0, 10));
		assertThat(pageOfReview).isNotEmpty();
		assertThat(pageOfReview.getContent().get(0).getSubject()).isEqualTo(SUBJECT);
		assertThat(pageOfReview.getContent().get(0).getContent()).isEqualTo(CONTENT);
	}
	
	@After
	public void after() {
		
		Optional<ReviewEntity> optOfReview = reviewRepository.findById(ID);
		assertThat(optOfReview.isPresent()).isTrue();
		
		Optional<ReviewEntity> pageOfReview = reviewRepository.findByReviewID(TestingInitialization.DEFAULT_REVIEW_ID);
		assertThat(pageOfReview.isPresent()).isTrue();
	}
}
