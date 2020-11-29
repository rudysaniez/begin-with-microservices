package com.me.microservices.core.review.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.system.OutputCaptureRule;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.me.api.Api;
import com.me.api.core.review.Review;
import com.me.api.event.Event;
import com.me.microservices.core.review.repository.ReactiveReviewRepository;
import com.me.microservices.core.review.repository.ReviewRepository;
import com.me.microservices.core.review.service.AsciiArtService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
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
	
	@Autowired
	private Sink channel;
	
	@Rule
	public OutputCaptureRule output = new OutputCaptureRule();
	
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
		
		asciiArt.display("GET REVIEW");
		
		/**
		 * Get by reviewID.
		 */
		getAndVerifyStatus(REVIEW_ID, HttpStatus.OK).
			jsonPath("$.content").isEqualTo(CONTENT);
		
		/**
		 * Get review by productID.
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
	public void getReviewNotFoundException() {
		
		asciiArt.display("GET REVIEW BUT NOT FOUND EXCEPTION");
		
		deleteAndVerifyStatus(REVIEW_ID, HttpStatus.OK);
		
		getAndVerifyStatus(REVIEW_ID, HttpStatus.NOT_FOUND).
			jsonPath("$.message").isEqualTo(String.format("Review with reviewID=%d doesn't not exists.", REVIEW_ID));
	}
	
	@Test
	public void getReviewInvalidInputException() {
		
		asciiArt.display("GET REVIEW BUT INVALID INPUT EXCEPTION");
		
		getAndVerifyStatus(0, HttpStatus.UNPROCESSABLE_ENTITY).
			jsonPath("$.message").isEqualTo("ReviewID should be greater than 0.");
	}
	
	@Test
	public void createReview() {
		
		asciiArt.display("CREATE REVIEW");
		
		IntStream.rangeClosed(50, 80).mapToObj(i -> new Review(i, 2, AUTHOR + "_" + i, SUBJECT + "_" + i, CONTENT)).
			forEach(review -> createAndVerifyStatus(review, HttpStatus.CREATED));
		
		getAndVerifyStatus(50, HttpStatus.OK).
			jsonPath("$.author").isEqualTo(AUTHOR + "_" + 50);
	}
	
	@Test
	public void createReviewDataIntegrityViolationException() {
		
		asciiArt.display("CREATE REVIEW BUT DATA INTEGRITY VIOLATION EXCEPTION");
		
		createAndVerifyStatus(new Review(REVIEW_ID, PRODUCT_ID, AUTHOR, SUBJECT, CONTENT), HttpStatus.UNPROCESSABLE_ENTITY).
			jsonPath("$.message").isEqualTo(String.format("Duplicate key : check the reviewID (%d).", REVIEW_ID));
	}
	
	@Test
	public void createReviewInvalidInputException() {
		
		asciiArt.display("CREATE REVIEW BUT INVALID INPUT EXCEPTION");
		
		createAndVerifyStatus(new Review(0, PRODUCT_ID, AUTHOR, SUBJECT, CONTENT), HttpStatus.UNPROCESSABLE_ENTITY).
			jsonPath("$.message").isEqualTo("ReviewID should be greater than 0.");
	}
	
	@Test
	public void deleteReview() {
		
		asciiArt.display("DELETE REVIEW BY PRODUCT ID");
		
		deleteAndVerifyStatus(PRODUCT_ID, HttpStatus.OK);
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>(3);
		params.add("productId", PRODUCT_ID.toString());
		params.add("pageNumber", "0");
		params.add("pageSize", "5");
		
		getAndVerifyStatus(params, HttpStatus.OK).
			jsonPath("$.content.length()").isEqualTo(0);
	}
	
	@Test
	public void deleteReviewBadRequest() {
		
		asciiArt.display("DELETE REVIEW BUT BAD REQUEST");
		
		deleteAndVerifyStatus(null, HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void deleteReviewInvalidInputException() {
		
		asciiArt.display("DELETE REVIEW BUT INVALID INPUT");
		
		deleteAndVerifyStatus(0, HttpStatus.UNPROCESSABLE_ENTITY).
			jsonPath("$.message").isEqualTo("ProductID should be greater than 0.");
	}
	
	@Test
	public void deleteReviewAsynchronous() {
		
		asciiArt.display("DELETE REVIEW SYNCHRONOUS");
		
		sendDeleteReviewEvent(PRODUCT_ID);
		assertThat(reviewRepository.findByProductID(PRODUCT_ID, PageRequest.of(0, 10)).getContent()).isEmpty();
		
		assertThat(output).contains(String.format(" > The review(s) with productID=%d has been deleted", PRODUCT_ID));
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
	
	/**
	 * @param reviewId
	 */
	public void sendDeleteReviewEvent(Integer reviewId) {
		
		Event<Integer, Review> event = new Event<>(reviewId, null, Event.Type.DELETE);
		log.info(" > One message will be sent for a review deletion ({}).", event.toString());
		channel.input().send(MessageBuilder.withPayload(event).build());
	}
}
