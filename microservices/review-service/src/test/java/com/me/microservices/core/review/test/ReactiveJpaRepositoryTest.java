package com.me.microservices.core.review.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.me.handler.exception.NotFoundException;
import com.me.microservices.core.review.bo.ReviewEntity;
import com.me.microservices.core.review.repository.ReactiveJpaRepositoryImpl;
import com.me.microservices.core.review.service.AsciiArtService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AutoConfigureTestDatabase(connection=EmbeddedDatabaseConnection.HSQL)
@Transactional
@Commit
@DataJpaTest
@RunWith(SpringRunner.class)
public class ReactiveJpaRepositoryTest {
	
	@Autowired
	ReactiveJpaRepositoryImpl<ReviewEntity, Integer> repo;
	
	@Autowired
	private AsciiArtService asciiArt;
	
	private static final Integer REVIEW_ID = 1;
	private static final Integer PRODUCT_ID = 1;
	private static final String SUBJECT = "Washing machine";
	private static final String CONTENT = "Good product. The installation is simply.";
	private static final String AUTHOR = "rudysaniez";
	
	private static ReviewEntity savedEntity = null;

	@Before
	public void setup() {
		
		asciiArt.display("SETUP");
		
		repo.deleteAllEntities().block();
		
		assertFalse(repo.existsById(REVIEW_ID).block());
		
		ReviewEntity entity = new ReviewEntity(REVIEW_ID, PRODUCT_ID, AUTHOR, SUBJECT, CONTENT);
		entity = repo.save(entity).block();
		assertNotNull(entity); savedEntity = entity;
		
		assertNotNull(repo.findById(entity.getId()));
		assertEquals(1, repo.count().block());
	}
	
	@Test
	public void findById() {
		
		asciiArt.display("FIND  BY  ID");
		
		assertNotNull(repo.findById(savedEntity.getId()).block());
	}
	
	@Test(expected=NotFoundException.class)
	public void findAndNotFoundException() {
		
		asciiArt.display("FIND  BY  ID  AND  NOT  FOUND  EXCEPTION");
		
		repo.findById(2).switchIfEmpty(Mono.error(new NotFoundException())).block(); //This review doesn't not exists.
	}
	
	@Test
	public void exists() {
		
		asciiArt.display("EXISTS");
		
		assertTrue(repo.existsById(savedEntity.getId()).block());
		
		repo.deleteEntityById(savedEntity.getId()).block();
		
		assertFalse(repo.existsById(savedEntity.getId()).block());
	}
	
	@Test
	public void existsWithPublisher() {
		
		asciiArt.display("EXISTS  WITH  PUBLISHER");
		
		assertTrue(repo.existsById(Mono.just(savedEntity.getId())).block());
	}

	@Test
	public void count() {
		
		asciiArt.display("COUNT");
		
		repo.saveAll(List.of(new ReviewEntity(REVIEW_ID + 1, PRODUCT_ID + 1, AUTHOR + "_1", SUBJECT + "_1", CONTENT + "_1"),
				new ReviewEntity(REVIEW_ID + 2, PRODUCT_ID + 2, AUTHOR + "_2", SUBJECT + "_2", CONTENT + "_2"),
				new ReviewEntity(REVIEW_ID + 3, PRODUCT_ID + 2, AUTHOR + "_3", SUBJECT + "_3", CONTENT + "_3"))).collectList().block();
		
		assertThat(repo.count().block()).isEqualTo(4);
		
	}
	
	@Ignore
	@Test
	public void deleteAllFromIterable() {
		
		asciiArt.display("DELETE  ALL  FROM  ITERABLE");
		
		repo.deleteAllEntities(List.of(savedEntity)).block();
		
		assertFalse(repo.existsById(savedEntity.getId()).block());
	}
	
	@Test
	public void findAll() {
		
		asciiArt.display("FIND  ALL");
		
		ReviewEntity entity = new ReviewEntity(2, 2, AUTHOR+"_2", SUBJECT+"_2", CONTENT+"_2");
		assertNotNull(repo.save(entity).block());
		
		assertThat(repo.findAll().collectList().block()).size().isEqualTo(2);
	}
	
	@Test
	public void save() {
		
		asciiArt.display("SAVE");
		
		ReviewEntity entity = new ReviewEntity(2, 2, AUTHOR+"_2", SUBJECT+"_2", CONTENT+"_2");
		assertNotNull(repo.save(entity).block());
	}
	
	@Test
	public void saveAllFromIterable() {
		
		List<ReviewEntity> reviews = repo.saveAll(List.of(new ReviewEntity(5, 5, AUTHOR+"_5", SUBJECT+"_5", CONTENT+"_5"),
				new ReviewEntity(6, 6, AUTHOR+"_6", SUBJECT+"_6", CONTENT+"_6"),
				new ReviewEntity(7, 7, AUTHOR+"_7", SUBJECT+"_7", CONTENT+"_7"))).collectList().block();
		
		assertThat(reviews).size().isEqualTo(3);
	}
	
	@Test
	public void saveAllFromPublisher() {
		
		Flux<ReviewEntity> fluxOfReviews = Flux.fromIterable(List.of(new ReviewEntity(2, 2, AUTHOR+"_2", SUBJECT+"_2", CONTENT+"_2"),
				new ReviewEntity(3, 3, AUTHOR+"_3", SUBJECT+"_3", CONTENT+"_3"),
				new ReviewEntity(4, 4, AUTHOR+"_4", SUBJECT+"_4", CONTENT+"_4")));
		
		List<ReviewEntity> reviews = repo.saveAll(fluxOfReviews).collectList().block();
		assertThat(reviews).size().isEqualTo(3);
	}
}
