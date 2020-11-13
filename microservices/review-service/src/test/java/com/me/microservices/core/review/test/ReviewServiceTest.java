package com.me.microservices.core.review.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;

import com.me.api.Api;
import com.me.api.core.review.Review;
import com.me.microservices.core.review.repository.ReviewRepository;

import reactor.core.publisher.Mono;

@AutoConfigureTestDatabase(connection=EmbeddedDatabaseConnection.HSQL)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class ReviewServiceTest {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private ReviewRepository reviewRepository;
	
	@Value("${spring.webflux.base-path}") 
	String basePath;
	
	private static final Integer REVIEW_ID = 1;
	private static final Integer PRODUCT_ID = 1;
	private static final String SUBJECT = "Washing machine";
	private static final String CONTENT = "Good product. The installation is simply.";
	private static final String AUTHOR = "rudysaniez";
	
	@Before
	public void setupdb() {
		
		reviewRepository.deleteAll();
		Review review = new Review(REVIEW_ID, PRODUCT_ID, AUTHOR, SUBJECT, CONTENT);
		createAndVerifyStatus(review, HttpStatus.CREATED);
	}
	
	@Test
	public void getReview() {
		
		getAndVerifyStatus(REVIEW_ID, HttpStatus.OK).
			jsonPath("$.content").isEqualTo(CONTENT);
	}
	
	/**
	 * @param reviewID
	 * @param status
	 * @return {@link BodyContentSpec}
	 */
	private BodyContentSpec getAndVerifyStatus(Integer reviewID, HttpStatus status) {
		
		return client.get().uri(basePath + "/" + Api.REVIEW_PATH + "/" + String.valueOf(reviewID)).
			accept(MediaType.APPLICATION_JSON).exchange().
			expectStatus().isEqualTo(status).
			expectBody();
	}
	
	/**
	 * @param body
	 * @param status
	 * @return {@link BodyContentSpec}
	 */
	private BodyContentSpec createAndVerifyStatus(Review body, HttpStatus status) {
		
		return client.post().uri(basePath + "/" + Api.REVIEW_PATH).
				body(Mono.just(body), Review.class).accept(MediaType.APPLICATION_JSON).exchange().
				expectStatus().isEqualTo(status).
				expectBody();
	}
}
