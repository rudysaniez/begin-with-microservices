package com.me.work.example.microservices.core.review.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.me.work.example.api.Api;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class ReviewTest {

	@Autowired
	private WebTestClient client;
	
	@Value("${spring.webflux.base-path}") String basePath;
	
	@Test
	public void getReview() {
		
		this.client.get().uri(this.basePath + "/" + Api.REVIEW_PATH + "/1").accept(MediaType.APPLICATION_JSON).exchange().
			expectStatus().is2xxSuccessful().
			expectBody().jsonPath("$.reviewID").isEqualTo("1");
	}
	
	@Test
	public void getReviewNotFound() {
		
		this.client.get().uri(this.basePath + "/" + Api.REVIEW_PATH + "/13").accept(MediaType.APPLICATION_JSON).exchange().
			expectStatus().is4xxClientError().
			expectBody().jsonPath("$.message").isEqualTo("The review 13 doesn't not exist").
			jsonPath("$.path").isEqualTo("/" + Api.REVIEW_PATH + "/13");
	}
	
	@Test
	public void getReviewInvalidInput() {
		
		this.client.get().uri(this.basePath + "/reviews/15").accept(MediaType.APPLICATION_JSON).exchange().
			expectStatus().is4xxClientError().
			expectBody().jsonPath("$.message").isEqualTo("The review 15 is an invalid input").
			jsonPath("$.path").isEqualTo("/" + Api.REVIEW_PATH + "/15");
	}
}
