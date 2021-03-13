package com.me.microservices.core.review.test;

import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.system.OutputCaptureRule;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.me.api.Api;
import com.me.api.event.Event;
import com.me.microservices.core.review.api.model.Review;
import com.me.microservices.core.review.repository.ReviewRepository;
import com.me.microservices.core.review.service.AsciiArtService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class ReviewServiceTest {

	@Autowired WebTestClient client;
	@Autowired ReviewRepository reviewRepository;
	@Autowired AsciiArtService asciiArt;
	
	@Autowired
	private Sink channel;
	
	@Rule
	public OutputCaptureRule output = new OutputCaptureRule();
	
	private static final Integer REVIEW_ID = 1;
	
	private static final Integer REVIEW_ID_BEGIN_PART1 = 2;
	private static final Integer REVIEW_ID_END_PART1 = 15;
	
	private static final Integer REVIEW_ID_NOT_FOUND = 999;
	private static final Integer REVIEW_ID_INVALID_INPUT = 0;
	
	private static final Integer PRODUCT_ID = 1;
	private static final Integer PRODUCT_ID_PART_1 = 1;
	
	private static final String SUBJECT = "Washing machine";
	private static final String CONTENT = "Good product. The installation is simply.";
	private static final String AUTHOR = "rudysaniez";
	
	@Before
	public void setupdb() {
		
		asciiArt.display("SETUP");
		
		reviewRepository.deleteAll().log().block();
		
		Review model = new Review().reviewID(REVIEW_ID).author(AUTHOR).
				content(CONTENT).productID(PRODUCT_ID).subject(SUBJECT);
		
		createAndVerifyStatus(model, HttpStatus.CREATED);
	}
	
	@Test
	public void getReviewById() {
		
		asciiArt.display("GET REVIEW");
		
		getAndVerifyStatus(REVIEW_ID, HttpStatus.OK).
			jsonPath("$.content").isEqualTo(CONTENT);
	}
	
	@Test
	public void getPagedReview() {
		
		asciiArt.display("GET PAGED REVIEW");
		
		IntStream.rangeClosed(REVIEW_ID_BEGIN_PART1, REVIEW_ID_END_PART1).
			mapToObj(i ->  new Review().reviewID(i).productID(PRODUCT_ID_PART_1).author(AUTHOR + "_" + i).
					subject(SUBJECT + "_" + i).content(CONTENT)).
			forEach(model -> createAndVerifyStatus(model, HttpStatus.CREATED));
	
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
		
		getAndVerifyStatus(REVIEW_ID_NOT_FOUND, HttpStatus.NOT_FOUND).
			jsonPath("$.message").isEqualTo(String.format("Review with reviewID=%d doesn't not exists.", REVIEW_ID_NOT_FOUND));
	}
	
	@Test
	public void getReviewInvalidInputException() {
		
		asciiArt.display("GET REVIEW BUT INVALID INPUT EXCEPTION");
		
		getAndVerifyStatus(0, HttpStatus.UNPROCESSABLE_ENTITY).
			jsonPath("$.message").isEqualTo("ReviewID should be greater than 0.");
	}
	
	@Test
	public void createReviewDataIntegrityViolationException() {
		
		asciiArt.display("CREATE REVIEW BUT DATA INTEGRITY VIOLATION EXCEPTION");
		
		Review review = new Review().reviewID(REVIEW_ID).productID(PRODUCT_ID).
				author(AUTHOR).subject(SUBJECT).content(CONTENT);
		
		createAndVerifyStatus(review, HttpStatus.UNPROCESSABLE_ENTITY).
			jsonPath("$.message").isEqualTo(String.format("Duplicate key : check the reviewID (%d).", REVIEW_ID));
	}
	
	@Test
	public void createReviewInvalidInputException() {
		
		asciiArt.display("CREATE REVIEW BUT INVALID INPUT EXCEPTION");
		
		Review review = new Review().reviewID(REVIEW_ID_INVALID_INPUT).productID(PRODUCT_ID).
				author(AUTHOR).subject(SUBJECT).content(CONTENT);
		
		createAndVerifyStatus(review, HttpStatus.UNPROCESSABLE_ENTITY).
			jsonPath("$.message").isEqualTo("ReviewID should be greater than 0.");
	}
	
	@Test
	public void deleteReview() {
		
		asciiArt.display("DELETE REVIEW BY PRODUCT ID");
		
		deleteAndVerifyStatus(PRODUCT_ID_PART_1, HttpStatus.OK);
	}
	
	@Test
	public void deleteReviewAsynchronous() {
		
		asciiArt.display("DELETE REVIEW SYNCHRONOUS");
		
		sendDeleteReviewEvent(PRODUCT_ID);
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
	
	/**
	 * @param reviewID
	 * @param status
	 * @return {@link BodyContentSpec}
	 */
	private BodyContentSpec getAndVerifyStatus(Integer reviewID, HttpStatus status) {
		
		return client.get().uri(uri -> uri.pathSegment("api", "v1", Api.REVIEW_PATH, String.valueOf(reviewID)).build()).
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
		
		return client.get().uri(uri -> uri.pathSegment("api", "v1", Api.REVIEW_PATH).queryParams(params).build()).
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
		
		return client.post().uri(uri -> uri.pathSegment("api", "v1", Api.REVIEW_PATH).build()).
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
		
		return client.delete().uri(uri -> uri.pathSegment("api", "v1", Api.REVIEW_PATH, String.valueOf(reviewID)).build()).
				accept(MediaType.APPLICATION_JSON).exchange().
				expectStatus().isEqualTo(status).
				expectBody();
	}
	
	/**
	 * @param reviewId
	 */
	public void sendDeleteReviewEvent(Integer reviewId) {
		
		Event<Integer> event = new Event<>(reviewId, Event.Type.DELETE);
		log.info(" > One message will be sent for a review deletion ({}).", event.toString());
		channel.input().send(MessageBuilder.withPayload(event).build());
	}
}
