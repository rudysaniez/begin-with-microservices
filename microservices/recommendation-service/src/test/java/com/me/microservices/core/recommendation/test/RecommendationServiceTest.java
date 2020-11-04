package com.me.microservices.core.recommendation.test;

import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.me.api.Api;
import com.me.api.core.recommendation.Recommendation;
import com.me.microservices.core.recommendation.bo.RecommendationEntity;
import com.me.microservices.core.recommendation.mapper.RecommendationMapper;
import com.me.microservices.core.recommendation.repository.RecommendationRepository;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class RecommendationServiceTest {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private RecommendationRepository recommendationRepository;
	
	@Autowired
	private RecommendationMapper mapper;
	
	@Value("${spring.webflux.base-path}") 
	private String basePath;
	
	private static final Integer RECOMMENDATION_ID = 1;
	private static final Integer PRODUCT_ID = 1;
	private static final String AUTHOR = "rudysaniez";
	private static final String CONTENT = "This product is very good !";
	private static final Integer RATE = 1;
	
	@Before
	public void setupdb() {
		
		recommendationRepository.deleteAll().block();
		
		Recommendation model = new Recommendation(RECOMMENDATION_ID, PRODUCT_ID, AUTHOR, RATE, CONTENT);
		
		createAndVerifyStatus(model, HttpStatus.CREATED).
			jsonPath("$.content").isEqualTo(CONTENT).
			jsonPath("$.author").isEqualTo(AUTHOR);
	}
	
	@Test
	public void getRecommendation() {
		
		getAndVerifyStatus(RECOMMENDATION_ID, HttpStatus.OK).
			jsonPath("$.recommendationID").isEqualTo(RECOMMENDATION_ID).
			jsonPath("$.author", AUTHOR);
	}
	
	@Test
	public void getRecommendationNotFound() {
		
		getAndVerifyStatus(13, HttpStatus.NOT_FOUND).
				jsonPath("$.message").isEqualTo(String.format("Recommendation with recommendationID=%d doesn't not exists.", 13)).
				jsonPath("$.path").isEqualTo("/" + Api.RECOMMENDATION_PATH + "/13");
	}
	
	@Test
	public void getRecommendationWithInvalidRecommendationID() {
		
		getAndVerifyStatus(0, HttpStatus.UNPROCESSABLE_ENTITY).
				jsonPath("$.message").isEqualTo("RecommendationID should be greater than 0").
				jsonPath("$.path").isEqualTo("/" + Api.RECOMMENDATION_PATH + "/0");
	}
	
	@Test
	public void findRecommendationByProductID() {
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>(3);
		params.add("productId", PRODUCT_ID.toString());
		params.add("pageNumber", "0");
		params.add("pageSize", "10");
		
		getAndVerifyStatus(params, HttpStatus.OK).
			jsonPath("$.content[0].author").isEqualTo(AUTHOR).
			jsonPath("$.page.number").isEqualTo(0).
			jsonPath("$.page.size").isEqualTo(10).
			jsonPath("$.page.totalPages").isEqualTo(1).
			jsonPath("$.page.totalElements").isEqualTo(1);
		
		IntStream.rangeClosed(2, 50).forEach(i -> 
			createAndVerifyStatus(new Recommendation(i, PRODUCT_ID, AUTHOR, RATE, CONTENT + "_" + i), HttpStatus.CREATED));
		
		getAndVerifyStatus(params, HttpStatus.OK).
			jsonPath("$.content[0].productID").isEqualTo(PRODUCT_ID).
			jsonPath("$.content[0].author").isEqualTo(AUTHOR).
			jsonPath("$.page.number").isEqualTo(0).
			jsonPath("$.page.totalElements", recommendationRepository.countByProductID(PRODUCT_ID));
		
		//Page 1.
		params = new LinkedMultiValueMap<>(3);
		params.add("productId", PRODUCT_ID.toString());
		params.add("pageNumber", "1");
		params.add("pageSize", "10");
		
		getAndVerifyStatus(params, HttpStatus.OK).
			jsonPath("$.content[0].recommendationID").isEqualTo(11).
			jsonPath("$.content[0].productID").isEqualTo(PRODUCT_ID).
			jsonPath("$.page.number").isEqualTo(1).
			jsonPath("$.page.totalElements").isEqualTo(recommendationRepository.countByProductID(PRODUCT_ID).block());
	}
	
	@Test
	public void saveRecommendation() {
		
		Recommendation entity = new Recommendation(2, PRODUCT_ID, AUTHOR, RATE, CONTENT);
		
		createAndVerifyStatus(entity, HttpStatus.CREATED).
			jsonPath("$.author").isEqualTo(AUTHOR).
			jsonPath("$.recommendationID").isEqualTo(2).
			jsonPath("$.productID").isEqualTo(PRODUCT_ID);
		
		StepVerifier.create(recommendationRepository.findByRecommendationID(2)).
			expectNextMatches(e -> e.getRecommendationID().equals(2)).verifyComplete();
	}
	
	@Test
	public void saveRecommendationDuplicateKey() {
		
		Recommendation model = new Recommendation(RECOMMENDATION_ID, PRODUCT_ID, AUTHOR, RATE, CONTENT);
		
		createAndVerifyStatus(model, HttpStatus.UNPROCESSABLE_ENTITY).
			jsonPath("$.message").isEqualTo(String.format("Duplicate key : check the recommendationID (%d).", RECOMMENDATION_ID));
	}
	
	@Test
	public void updateRecommendation() {
		
		RecommendationEntity entity = recommendationRepository.findByRecommendationID(RECOMMENDATION_ID).block();
		entity.setContent("Yes! good product! Nice and good conception.");
		
		updateAndVerifyStatus(RECOMMENDATION_ID, mapper.toModel(entity), HttpStatus.OK).
			jsonPath("$.content").isEqualTo("Yes! good product! Nice and good conception.");
	}
	
	@Test
	public void deleteRecommendation() {
		
		deleteRecommendation(RECOMMENDATION_ID, HttpStatus.OK);
		getAndVerifyStatus(RECOMMENDATION_ID, HttpStatus.NOT_FOUND);
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
}
