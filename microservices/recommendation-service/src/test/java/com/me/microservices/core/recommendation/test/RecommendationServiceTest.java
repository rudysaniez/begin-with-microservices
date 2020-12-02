package com.me.microservices.core.recommendation.test;

import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.me.api.core.recommendation.Recommendation;
import com.me.api.event.Event;
import com.me.microservices.core.recommendation.bo.RecommendationEntity;
import com.me.microservices.core.recommendation.mapper.RecommendationMapper;
import com.me.microservices.core.recommendation.repository.RecommendationRepository;
import com.me.microservices.core.recommendation.services.AsciiArtService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class RecommendationServiceTest {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private RecommendationRepository recommendationRepository;
	
	@Autowired
	private RecommendationMapper mapper;
	
	@Autowired
	private AsciiArtService asciiArt;
	
	@Value("${spring.webflux.base-path}") 
	private String basePath;
	
	@Autowired
	private Sink channel;
	
	@Rule
	public OutputCaptureRule output = new OutputCaptureRule();
	
	private static final Integer RECOMMENDATION_ID = 1;
	private static final Integer PRODUCT_ID = 1;
	private static final String AUTHOR = "rudysaniez";
	private static final String CONTENT = "This product is very good !";
	private static final Integer RATE = 1;
	
	@Before
	public void setupdb() {
		
		asciiArt.display("SETUP");
		
		recommendationRepository.deleteAll().block();
		
		Recommendation model = new Recommendation(RECOMMENDATION_ID, PRODUCT_ID, AUTHOR, RATE, CONTENT);
		
		createAndVerifyStatus(model, HttpStatus.CREATED).
			jsonPath("$.content").isEqualTo(CONTENT).
			jsonPath("$.author").isEqualTo(AUTHOR);
		
		IntStream.rangeClosed(RECOMMENDATION_ID + 1, 21).mapToObj(i -> new Recommendation(i, PRODUCT_ID, AUTHOR + "_" + i, RATE + i, CONTENT)).
			forEach(r -> createAndVerifyStatus(r, HttpStatus.CREATED));
	}
	
	@Test
	public void getRecommendation() {
		
		asciiArt.display("GET RECOMMENDATION");
		
		/**
		 * Get recommendation by recommendationID.
		 */
		getAndVerifyStatus(RECOMMENDATION_ID, HttpStatus.OK).
			jsonPath("$.recommendationID").isEqualTo(RECOMMENDATION_ID).
			jsonPath("$.author", AUTHOR);
		
		/**
		 * Get recommendation by productID.
		 */
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>(3);
		params.add("productId","1");
		params.add("pageNumber", "0");
		params.add("pageSize", "10");
		
		getAndVerifyStatus(params, HttpStatus.OK).
			jsonPath("$.content[0].author").isEqualTo(AUTHOR).
			jsonPath("$.page.number").isEqualTo(0).
			jsonPath("$.page.size").isEqualTo(10).
			jsonPath("$.page.totalElements").isEqualTo(recommendationRepository.countByProductID(PRODUCT_ID).block()).
			jsonPath("$.content[1].author").isEqualTo(AUTHOR + "_2");
		
		params = new LinkedMultiValueMap<>(3);
		params.add("productId","1");
		params.add("pageNumber", "1");
		params.add("pageSize", "10");
		
		getAndVerifyStatus(params, HttpStatus.OK).
			jsonPath("$.content[0].productID").isEqualTo(PRODUCT_ID).
			jsonPath("$.content[0].author").isEqualTo(AUTHOR + "_11").
			jsonPath("$.page.number").isEqualTo(1).
			jsonPath("$.page.totalElements", recommendationRepository.countByProductID(PRODUCT_ID));
	}
	
	@Test
	public void getRecommendationNotFoundException() {
		
		asciiArt.display("GET RECOMMENDATION NOT FOUND EXCEPTION");
		
		getAndVerifyStatus(999, HttpStatus.NOT_FOUND).
				jsonPath("$.message").isEqualTo(String.format("Recommendation with recommendationID=%d doesn't not exists.", 999)).
				jsonPath("$.path").isEqualTo("/" + Api.RECOMMENDATION_PATH + "/999");
	}
	
	@Test
	public void getRecommendationInvalidInputException() {
		
		asciiArt.display("GET RECOMMENDATION INVALID INPUT EXCEPTION");
		
		getAndVerifyStatus(0, HttpStatus.UNPROCESSABLE_ENTITY).
				jsonPath("$.message").isEqualTo("RecommendationID should be greater than 0").
				jsonPath("$.path").isEqualTo("/" + Api.RECOMMENDATION_PATH + "/0");
	}
	
	@Test
	public void createRecommendation() {
		
		asciiArt.display("CREATE RECOMMENDATION");
		
		Recommendation entity = new Recommendation(50, 2, AUTHOR + "_50", RATE, CONTENT);
		
		createAndVerifyStatus(entity, HttpStatus.CREATED).
			jsonPath("$.author").isEqualTo(AUTHOR + "_50").
			jsonPath("$.recommendationID").isEqualTo(50).
			jsonPath("$.productID").isEqualTo(2);
		
		StepVerifier.create(recommendationRepository.findByRecommendationID(50)).
			expectNextMatches(e -> e.getRecommendationID().equals(50)).verifyComplete();
	}
	
	@Test
	public void createRecommendationDuplicateKeyException() {
		
		asciiArt.display("CREATE RECOMMENDATION BUT DUPLICATE KEY EXCEPTION");
		
		Recommendation model = new Recommendation(RECOMMENDATION_ID, PRODUCT_ID, AUTHOR, RATE, CONTENT);
		
		createAndVerifyStatus(model, HttpStatus.UNPROCESSABLE_ENTITY).
			jsonPath("$.message").isEqualTo(String.format("Duplicate key : check the recommendationID (%d).", RECOMMENDATION_ID));
	}
	
	@Test
	public void updateRecommendation() {
		
		asciiArt.display("UPDATE RECOMMENDATION");
		
		RecommendationEntity entity = recommendationRepository.findByRecommendationID(RECOMMENDATION_ID).block();
		entity.setContent("Yes! good product! Nice and good conception.");
		
		updateAndVerifyStatus(RECOMMENDATION_ID, mapper.toModel(entity), HttpStatus.OK).
			jsonPath("$.content").isEqualTo("Yes! good product! Nice and good conception.");
	}
	
	@Test
	public void deleteRecommendation() {
		
		asciiArt.display("DELETE RECOMMENDATION");
		
		deleteRecommendation(PRODUCT_ID, HttpStatus.OK);
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>(3);
		params.add("productId",PRODUCT_ID.toString());
		params.add("pageNumber", "0");
		params.add("pageSize", "10");
		
		getAndVerifyStatus(params, HttpStatus.OK).
			jsonPath("$.content.length()").isEqualTo(0);
	}
	
	@Test
	public void deleteRecommendationAsynchronous() {
		
		asciiArt.display("DELETE RECOMMENDATION ASYNCHRONOUS");
		
		sendDeleteRecommendationEvent(PRODUCT_ID);
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>(3);
		params.add("productId",PRODUCT_ID.toString());
		params.add("pageNumber", "0");
		params.add("pageSize", "10");
		
		getAndVerifyStatus(params, HttpStatus.OK).
			jsonPath("$.content.length()").isEqualTo(0);
	}
	
	@Test
	public void deleteRecommendationBadRequest() {
		
		asciiArt.display("DELETE RECOMMENDATION BUT BAD REQUEST");
		
		deleteRecommendation(null, HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void deleteRecommendationInvalidInputException() {
		
		asciiArt.display("DELETE RECOMMENDATION BUT INVALID INPUT EXCEPTION");
		
		deleteRecommendation(0, HttpStatus.UNPROCESSABLE_ENTITY);
	}


	/**
	 * @param recommendationID
	 * @param status
	 * @return {@link BodyContentSpec}
	 */
	private BodyContentSpec getAndVerifyStatus(Integer recommendationID, HttpStatus status) {
		
		return client.get().uri(basePath + "/" + Api.RECOMMENDATION_PATH + "/" + String.valueOf(recommendationID)).
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
		
		return client.get().uri(uri -> uri.path(basePath + "/" + Api.RECOMMENDATION_PATH).queryParams(params).build()).
				accept(MediaType.APPLICATION_JSON).exchange().
				expectStatus().isEqualTo(status).
				expectBody();
	}
	
	/**
	 * @param body
	 * @param status
	 * @return {@link BodyContentSpec}
	 */
	private BodyContentSpec createAndVerifyStatus(Recommendation body, HttpStatus status) {
		
		return client.post().uri(basePath + "/" + Api.RECOMMENDATION_PATH).
				body(Mono.just(body), Recommendation.class).accept(MediaType.APPLICATION_JSON).exchange().
				expectStatus().isEqualTo(status).
				expectBody();
	}
	
	/**
	 * @param body
	 * @param status
	 * @return {@link BodyContentSpec}
	 */
	private BodyContentSpec updateAndVerifyStatus(Integer recommendationID, Recommendation body, HttpStatus status) {
		
		return client.put().uri(basePath + "/" + Api.RECOMMENDATION_PATH + "/" + recommendationID).
				body(Mono.just(body), Recommendation.class).accept(MediaType.APPLICATION_JSON).exchange().
				expectStatus().isEqualTo(status).
				expectBody();
	}
	
	/**
	 * @param recommendationID
	 */
	private void deleteRecommendation(Integer recommendationID, HttpStatus status) {
		
		client.delete().uri(basePath + "/" + Api.RECOMMENDATION_PATH + "/" + recommendationID).
			accept(MediaType.APPLICATION_JSON).exchange().
			expectStatus().isEqualTo(status);
	}
	
	/**
	 * @param recommendationId
	 */
	public void sendDeleteRecommendationEvent(Integer recommendationId) {
		
		Event<Integer> event = new Event<>(recommendationId, Event.Type.DELETE);
		log.info(" > One message will be sent for a recommendation deletion ({}).", event.toString());
		channel.input().send(MessageBuilder.withPayload(event).build());
	}
}
