package com.me.work.example.microservices.core.review.test;

import static reactor.core.publisher.Mono.just;

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

import com.me.work.example.api.Api;
import com.me.work.example.api.core.review.Review;
import com.me.work.example.microservices.core.review.bo.ReviewEntity;
import com.me.work.example.microservices.core.review.mapper.ReviewMapper;
import com.me.work.example.microservices.core.review.repository.ReviewRepository;

@AutoConfigureTestDatabase(connection=EmbeddedDatabaseConnection.HSQL)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class ReviewServiceTest {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private ReviewMapper mapper;
	
	@Autowired
	private ReviewRepository reviewRepository;
	
	@Value("${spring.webflux.base-path}") 
	String basePath;
	
	private static final Integer REVIEW_ID = 999;
	private static final Integer PRODUCT_ID = 999;
	private static final String SUBJECT = "Washing machine";
	private static final String CONTENT = "Good product. The installation is simply.";
	private static final String AUTHOR = "rudysaniez";
	
	@Before
	public void setupdb() {
		
		reviewRepository.deleteAll();
		
		ReviewEntity entity = new ReviewEntity(REVIEW_ID, PRODUCT_ID, AUTHOR, SUBJECT, CONTENT);
		
		client.post().uri(basePath + "/" + Api.REVIEW_PATH).accept(MediaType.APPLICATION_JSON).
			body(just(mapper.toModel(entity)), Review.class).exchange().
				expectStatus().isEqualTo(HttpStatus.CREATED).
				expectBody().
					jsonPath("$.reviewID").isEqualTo(REVIEW_ID);
	}
	
	@Test
	public void getReview() {
		
		client.get().uri(basePath + "/" + Api.REVIEW_PATH + "/" + REVIEW_ID).accept(MediaType.APPLICATION_JSON).exchange().
			expectStatus().is2xxSuccessful().
			expectBody().jsonPath("$.reviewID").isEqualTo(REVIEW_ID);
	}
	
	@Test
	public void getReviewNotFound() {
		
		client.get().uri(basePath + "/" + Api.REVIEW_PATH + "/13").accept(MediaType.APPLICATION_JSON).exchange().
			expectStatus().is4xxClientError().
			expectBody().
				jsonPath("$.message").isEqualTo("The review with reviewID=13 doesn't not exists.").
				jsonPath("$.path").isEqualTo("/" + Api.REVIEW_PATH + "/13");
	}
	
	@Test
	public void getReviewWithInvalidReviewID() {
		
		client.get().uri(basePath + "/" + Api.REVIEW_PATH + "/0").accept(MediaType.APPLICATION_JSON).exchange().
			expectStatus().is4xxClientError().
				expectBody().
					jsonPath("$.message").isEqualTo("ReviewID should be greater than 0").
					jsonPath("$.path").isEqualTo("/" + Api.REVIEW_PATH + "/0");
	}
	
	@Test
	public void findReviewByProductID() {
		
		client.get().uri( uri -> uri.path(basePath + "/" + Api.REVIEW_PATH).queryParam("productId", PRODUCT_ID).build()).
			accept(MediaType.APPLICATION_JSON).exchange().
				expectStatus().isEqualTo(HttpStatus.OK).
				expectBody().
					jsonPath("$.content[0].reviewID").isEqualTo(REVIEW_ID);
	}
	
	@Test
	public void saveReview() {
		
		ReviewEntity entity = new ReviewEntity(1, 1, AUTHOR, SUBJECT, CONTENT);
		
		client.post().uri(uri -> uri.path(basePath + "/" + Api.REVIEW_PATH).build()).body(just(mapper.toModel(entity)), Review.class).
			accept(MediaType.APPLICATION_JSON).exchange().
				expectStatus().isEqualTo(HttpStatus.CREATED).
				expectBody().
					jsonPath("$.reviewID").isEqualTo(1);
	}
	
	@Test
	public void updateReview() {
		
		ReviewEntity entity = new ReviewEntity(999, 999, AUTHOR, SUBJECT, 
				"This product is well, but... no good!");
		
		client.put().uri(basePath + "/" + Api.REVIEW_PATH + "/" + REVIEW_ID).body(just(mapper.toModel(entity)), Review.class).
			accept(MediaType.APPLICATION_JSON).exchange().
				expectStatus().isEqualTo(HttpStatus.OK).
				expectBody().
					jsonPath("$.content").isEqualTo("This product is well, but... no good!");
	}
	
	@Test
	public void deleteReview() {
		
		client.delete().uri(basePath + "/" + Api.REVIEW_PATH + "/" + REVIEW_ID).accept(MediaType.APPLICATION_JSON).exchange().
			expectStatus().isEqualTo(HttpStatus.OK);
		
		client.get().uri(basePath + "/" + Api.REVIEW_PATH + "/" + REVIEW_ID).accept(MediaType.APPLICATION_JSON).exchange().
			expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
	}
}
