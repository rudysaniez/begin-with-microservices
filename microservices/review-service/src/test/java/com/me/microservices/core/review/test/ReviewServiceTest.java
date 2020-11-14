package com.me.microservices.core.review.test;

import java.util.stream.IntStream;

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
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.me.api.Api;
import com.me.api.core.review.Review;
import com.me.microservices.core.review.repository.ReactiveReviewRepository;
import com.me.microservices.core.review.repository.ReviewRepository;
import com.me.microservices.core.review.service.AsciiArtService;

import reactor.core.publisher.Mono;

@AutoConfigureTestDatabase(connection=EmbeddedDatabaseConnection.HSQL)
@RunWith(SpringRunner.class)
@Transactional
@Commit
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class ReviewServiceTest {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private ReviewRepository reviewRepository;
	
	@Autowired
	private ReactiveReviewRepository reactiveReviewRepository;
	
	@Autowired
	private AsciiArtService asciiArt;
	
	@Value("${spring.webflux.base-path}") 
	String basePath;
	
	private static final Integer REVIEW_ID = 1;
	private static final Integer PRODUCT_ID = 1;
	private static final String SUBJECT = "Washing machine";
	private static final String CONTENT = "Good product. The installation is simply.";
	private static final String AUTHOR = "rudysaniez";
	
	@Before
	public void setupdb() {
		
		asciiArt.display("SETUP");
		
		reactiveReviewRepository.deleteAllEntities().block();
		
		Review review = new Review(REVIEW_ID, PRODUCT_ID, AUTHOR, SUBJECT, CONTENT);
		createAndVerifyStatus(review, HttpStatus.CREATED);
		
		IntStream.rangeClosed(REVIEW_ID + 1, 21).mapToObj(i -> new Review(i, PRODUCT_ID, AUTHOR + "_" + i, SUBJECT + "_" + i, CONTENT)).
			forEach(entity -> createAndVerifyStatus(entity, HttpStatus.CREATED));
	}
	
	@Test
	public void getReview() {
		
		asciiArt.display("GET  REVIEW");
		
		/**
		 * Get by ID.
		 */
		getAndVerifyStatus(REVIEW_ID, HttpStatus.OK).
			jsonPath("$.content").isEqualTo(CONTENT);
		
		/**
		 * Get page by productID.
		 */
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>(3);
		params.add("productId", "1");
		params.add("pageNumber", "0");
		params.add("pageSize", "5");
		
		getAndVerifyStatus(params, HttpStatus.OK).
			jsonPath("$.content[0].author").isEqualTo(AUTHOR).
			jsonPath("$.page.totalElements", reviewRepository.count());
		
		params = new LinkedMultiValueMap<>(3);
		params.add("productId", "1");
		params.add("pageNumber", "1");
		params.add("pageSize", "10");
		
		getAndVerifyStatus(params, HttpStatus.OK).
			jsonPath("$.content[0].author").isEqualTo(AUTHOR + "_11").
			jsonPath("$.content[0].reviewID").isEqualTo(REVIEW_ID + 10);
		
	}
	
	@Test
	public void createReview() {
		
		asciiArt.display("CREATE  REVIEW");
		
		IntStream.rangeClosed(50, 80).mapToObj(i -> new Review(i, 2, AUTHOR + "_" + i, SUBJECT + "_" + i, CONTENT)).
			forEach(review -> createAndVerifyStatus(review, HttpStatus.CREATED));
		
		getAndVerifyStatus(50, HttpStatus.OK).
			jsonPath("$.author").isEqualTo(AUTHOR + "_" + 50);
	}
	
	@Test
	public void createReviewDataIntegrityViolationException() {
		
		asciiArt.display("CREATE  REVIEW  BUT  DATA  INTEGRITY  VIOLATION  EXCEPTION");
		
		createAndVerifyStatus(new Review(REVIEW_ID, PRODUCT_ID, AUTHOR, SUBJECT, CONTENT), HttpStatus.UNPROCESSABLE_ENTITY).
			jsonPath("$.message").isEqualTo(String.format("Duplicate key : check the reviewID (%d).", REVIEW_ID));
	}
	
	@Test
	public void getReviewNotFoundException() {
		
		asciiArt.display("GET  REVIEW  BUT  NOT  FOUND  EXCEPTION");
		
		deleteAndVerifyStatus(REVIEW_ID, HttpStatus.OK);
		
		getAndVerifyStatus(REVIEW_ID, HttpStatus.NOT_FOUND).
			jsonPath("$.message").isEqualTo(String.format("Review with reviewID=%d doesn't not exists.", REVIEW_ID));
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
	 * @param params
	 * @param status
	 * @return {@link BodyContentSpec}
	 */
	private BodyContentSpec getAndVerifyStatus(MultiValueMap<String, String> params, HttpStatus status) {
		
		return client.get().uri(uri -> uri.path(basePath + "/" + Api.REVIEW_PATH).queryParams(params).build()).
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
	
	/**
	 * @param reviewID
	 * @param status
	 * @return {@link BodyContentSpec}
	 */
	private BodyContentSpec deleteAndVerifyStatus(Integer reviewID, HttpStatus status) {
		
		return client.delete().uri(basePath + "/" + Api.REVIEW_PATH + "/" + reviewID).
				accept(MediaType.APPLICATION_JSON).exchange().
				expectStatus().isEqualTo(status).
				expectBody();
	}
}
