package com.me.microservices.core.recommendation.test;

import static reactor.core.publisher.Mono.just;

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

import com.me.api.Api;
import com.me.api.core.recommendation.Recommendation;
import com.me.handler.exception.NotFoundException;
import com.me.microservices.core.recommendation.bo.RecommendationEntity;
import com.me.microservices.core.recommendation.mapper.RecommendationMapper;
import com.me.microservices.core.recommendation.repository.RecommendationRepository;

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
	
	private static final Integer RECOMMENDATION_ID = 999;
	private static final Integer PRODUCT_ID = 999;
	private static final String AUTHOR = "rudysaniez";
	private static final String CONTENT = "This product is very good !";
	private static final Integer RATE = 1;
	
	@Before
	public void setupdb() {
		
		recommendationRepository.deleteAll();
		RecommendationEntity entity = new RecommendationEntity(RECOMMENDATION_ID, PRODUCT_ID, AUTHOR, RATE, CONTENT);
		
		client.post().uri(basePath + "/" + Api.RECOMMENDATION_PATH).accept(MediaType.APPLICATION_JSON).
			body(just(mapper.toModel(entity)), Recommendation.class).exchange().
				expectStatus().isEqualTo(HttpStatus.CREATED).
				expectBody().jsonPath("$.recommendationID").isEqualTo(RECOMMENDATION_ID);
	}
	
	@Test
	public void getRecommendation() {
		
		this.client.get().uri(basePath + "/" + Api.RECOMMENDATION_PATH + "/" + RECOMMENDATION_ID).
			accept(MediaType.APPLICATION_JSON).exchange().
				expectStatus().is2xxSuccessful().
				expectBody().
					jsonPath("$.recommendationID").isEqualTo(RECOMMENDATION_ID).
					jsonPath("$.author", AUTHOR);
	}
	
	@Test
	public void getRecommendationNotFound() {
		
		client.get().uri(basePath + "/" + Api.RECOMMENDATION_PATH + "/13").accept(MediaType.APPLICATION_JSON).exchange().
			expectStatus().is4xxClientError().
			expectBody().
				jsonPath("$.message").isEqualTo(String.format("The recommendation with recommendationID=%d doesn't not exists.", 13)).
				jsonPath("$.path").isEqualTo("/" + Api.RECOMMENDATION_PATH + "/13");
	}
	
	@Test
	public void getRecommendationWithInvalidRecommendationID() {
		
		client.get().uri(basePath + "/" + Api.RECOMMENDATION_PATH + "/0").accept(MediaType.APPLICATION_JSON).exchange().
			expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY).
			expectBody().
				jsonPath("$.message").isEqualTo("RecommendationID should be greater than 0").
				jsonPath("$.path").isEqualTo("/" + Api.RECOMMENDATION_PATH + "/0");
	}
	
	@Test
	public void findRecommendationByProductID() {
		
		client.get().uri(
				uriBuilder -> uriBuilder.path(basePath + "/" + Api.RECOMMENDATION_PATH).
					queryParam("productId", PRODUCT_ID).build()).accept(MediaType.APPLICATION_JSON).exchange().
			
					expectStatus().isEqualTo(HttpStatus.OK).
					expectBody().
						jsonPath("$.content[0].author").isEqualTo(AUTHOR);
	}
	
	@Test
	public void saveRecommendation() {
		
		RecommendationEntity entity = new RecommendationEntity(1, 1, AUTHOR, RATE, CONTENT);
		
		client.post().uri(basePath + "/" + Api.RECOMMENDATION_PATH).body(just(mapper.toModel(entity)), Recommendation.class).
			accept(MediaType.APPLICATION_JSON).exchange().
				expectStatus().isEqualTo(HttpStatus.CREATED).
				expectBody().
					jsonPath("$.author").isEqualTo(AUTHOR).
					jsonPath("$.recommendationID").isEqualTo(1).
					jsonPath("$.productID").isEqualTo(1);
	}
	
	@Test
	public void saveRecommendationDuplicateKey() {
		
		RecommendationEntity entity = new RecommendationEntity(RECOMMENDATION_ID, PRODUCT_ID, AUTHOR, RATE, CONTENT);
		
		client.post().uri(basePath + "/" + Api.RECOMMENDATION_PATH).accept(MediaType.APPLICATION_JSON).
			body(just(mapper.toModel(entity)), Recommendation.class).exchange().
				expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY).
					expectBody().
						jsonPath("$.message").isEqualTo(String.format("Duplicate key : check the recommendationID (%d).", RECOMMENDATION_ID));
	}
	
	@Test
	public void updateRecommendation() {
		
		RecommendationEntity entity = recommendationRepository.findByRecommendationID(RECOMMENDATION_ID).
				orElseThrow(() -> new NotFoundException());
		
		entity.setContent("Yes! good product! Nice and good conception.");
		
		client.put().uri(basePath + "/" + Api.RECOMMENDATION_PATH + "/" + RECOMMENDATION_ID).accept(MediaType.APPLICATION_JSON).
			body(just(mapper.toModel(entity)), Recommendation.class).exchange().
				expectStatus().isEqualTo(HttpStatus.OK).
				expectBody().
					jsonPath("$.content").isEqualTo("Yes! good product! Nice and good conception.");
	}
	
	@Test
	public void deleteRecommendation() {
		
		client.delete().uri(basePath + "/" + Api.RECOMMENDATION_PATH + "/" + RECOMMENDATION_ID).exchange().
			expectStatus().is2xxSuccessful();
	}
}
