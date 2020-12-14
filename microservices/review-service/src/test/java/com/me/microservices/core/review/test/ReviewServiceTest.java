package com.me.microservices.core.review.test;

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
import com.me.api.event.Event;
import com.me.microservices.core.review.api.model.Review;
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
	
	private static final Integer REVIEW_ID_PART1 = 2;
	private static final Integer REVIEW_ID_PART2 = 16;
	private static final Integer REVIEW_ID_PART3 = 26;
	private static final Integer REVIEW_ID_NOT_FOUND = 999;
	private static final Integer REVIEW_ID_INVALID_INPUT = 0;
	
	private static final Integer PRODUCT_ID = 1;
	private static final Integer PRODUCT_ID_PART_1 = 1;
	private static final Integer PRODUCT_ID_PART_2 = 2;
	private static final Integer PRODUCT_ID_PART_3 = 3;
	
	
	private static final String SUBJECT = "Washing machine";
	private static final String CONTENT = "Good product. The installation is simply.";
	private static final String AUTHOR = "rudysaniez";
	
	@Before
	public void setupdb() {
		
		asciiArt.display("SETUP");
		
		reactiveReviewRepository.deleteAllEntities().block();
		
		Review review = ReviewModelBuilder.create().withReviewID(REVIEW_ID).withProductID(PRODUCT_ID).
			withAuthor(AUTHOR).withSubject(SUBJECT).withContent(CONTENT).build();
		
		createAndVerifyStatus(review, HttpStatus.CREATED);
		
		IntStream.rangeClosed(REVIEW_ID_PART1, 15).
			mapToObj(i ->  ReviewModelBuilder.create().withReviewID(i).withProductID(PRODUCT_ID_PART_1).withAuthor(AUTHOR + "_" + i).withSubject(SUBJECT + "_" + i).withContent(CONTENT).build()).
			forEach(entity -> createAndVerifyStatus(entity, HttpStatus.CREATED));
		
		IntStream.rangeClosed(REVIEW_ID_PART2, 25).
			mapToObj(i ->  ReviewModelBuilder.create().withReviewID(i).withProductID(PRODUCT_ID_PART_2).withAuthor(AUTHOR + "_" + i).withSubject(SUBJECT + "_" + i).withContent(CONTENT).build()).
			forEach(entity -> createAndVerifyStatus(entity, HttpStatus.CREATED));
		
		IntStream.rangeClosed(REVIEW_ID_PART3, 35).
			mapToObj(i ->  ReviewModelBuilder.create().withReviewID(i).withProductID(PRODUCT_ID_PART_3).withAuthor(AUTHOR + "_" + i).withSubject(SUBJECT + "_" + i).withContent(CONTENT).build()).
			forEach(entity -> createAndVerifyStatus(entity, HttpStatus.CREATED));
	}
	
	@Test
	public void crudTest() {

		getReviewById();
		getPagedReview();
		getReviewNotFoundException();
		getReviewInvalidInputException();
		
		createReviewDataIntegrityViolationException();
		createReviewInvalidInputException();
		
		deleteReview();
		deleteReviewAsynchronous();
		deleteReviewBadRequest();
		deleteReviewInvalidInputException();
	}
	
	private void getReviewById() {
		
		asciiArt.display("GET REVIEW");
		
		getAndVerifyStatus(REVIEW_ID, HttpStatus.OK).
			jsonPath("$.content").isEqualTo(CONTENT);
	}
	
	private void getPagedReview() {
		
		asciiArt.display("GET PAGED REVIEW");
		
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
	
	private void getReviewNotFoundException() {
		
		asciiArt.display("GET REVIEW BUT NOT FOUND EXCEPTION");
		
		getAndVerifyStatus(REVIEW_ID_NOT_FOUND, HttpStatus.NOT_FOUND).
			jsonPath("$.message").isEqualTo(String.format("Review with reviewID=%d doesn't not exists.", REVIEW_ID_NOT_FOUND));
	}
	
	private void getReviewInvalidInputException() {
		
		asciiArt.display("GET REVIEW BUT INVALID INPUT EXCEPTION");
		
		getAndVerifyStatus(0, HttpStatus.UNPROCESSABLE_ENTITY).
			jsonPath("$.message").isEqualTo("ReviewID should be greater than 0.");
	}
	
	private void createReviewDataIntegrityViolationException() {
		
		asciiArt.display("CREATE REVIEW BUT DATA INTEGRITY VIOLATION EXCEPTION");
		
		Review review = ReviewModelBuilder.create().withReviewID(REVIEW_ID_PART3).withProductID(PRODUCT_ID).
				withAuthor(AUTHOR).withSubject(SUBJECT).withContent(CONTENT).build();
		
		createAndVerifyStatus(review, HttpStatus.UNPROCESSABLE_ENTITY).
			jsonPath("$.message").isEqualTo(String.format("Duplicate key : check the reviewID (%d).", REVIEW_ID_PART3));
	}
	
	private void createReviewInvalidInputException() {
		
		asciiArt.display("CREATE REVIEW BUT INVALID INPUT EXCEPTION");
		
		Review review = ReviewModelBuilder.create().withReviewID(REVIEW_ID_INVALID_INPUT).withProductID(PRODUCT_ID).
				withAuthor(AUTHOR).withSubject(SUBJECT).withContent(CONTENT).build();
		
		createAndVerifyStatus(review, HttpStatus.UNPROCESSABLE_ENTITY).
			jsonPath("$.message").isEqualTo("ReviewID should be greater than 0.");
	}
	
	private void deleteReview() {
		
		asciiArt.display("DELETE REVIEW BY PRODUCT ID");
		
		deleteAndVerifyStatus(PRODUCT_ID_PART_1, HttpStatus.OK);
	}
	
	private void deleteReviewAsynchronous() {
		
		asciiArt.display("DELETE REVIEW SYNCHRONOUS");
		
		sendDeleteReviewEvent(PRODUCT_ID_PART_2);
	}
	
	private void deleteReviewBadRequest() {
		
		asciiArt.display("DELETE REVIEW BUT BAD REQUEST");
		
		deleteAndVerifyStatus(null, HttpStatus.BAD_REQUEST);
	}
	
	private void deleteReviewInvalidInputException() {
		
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
		
		Event<Integer> event = new Event<>(reviewId, Event.Type.DELETE);
		log.info(" > One message will be sent for a review deletion ({}).", event.toString());
		channel.input().send(MessageBuilder.withPayload(event).build());
	}
	
	public static class ReviewModelBuilder {
		
		private Integer reviewID;
		private Integer productID;
		private String author;
		private String subject;
		private String content;
		
		private ReviewModelBuilder() {}
		
		public static ReviewModelBuilder create() {
			return new ReviewModelBuilder();
		}
		
		public ReviewModelBuilder withReviewID(Integer reviewID) {
			this.reviewID = reviewID;
			return this;
		}
		
		public ReviewModelBuilder withProductID(Integer productID) {
			this.productID = productID;
			return this;
		}
		
		public ReviewModelBuilder withAuthor(String author) {
			this.author = author;
			return this;
		}
		
		public ReviewModelBuilder withSubject(String subject) {
			this.subject = subject;
			return this;
		}
		
		public ReviewModelBuilder withContent(String content) {
			this.content = content;
			return this;
		}
		
		public Review build() {
			
			Review review = new Review();
			review.setAuthor(author);
			review.setContent(content);
			review.setProductID(productID);
			review.setReviewID(reviewID);
			review.setSubject(subject);
			return review;
		}
	}
}
