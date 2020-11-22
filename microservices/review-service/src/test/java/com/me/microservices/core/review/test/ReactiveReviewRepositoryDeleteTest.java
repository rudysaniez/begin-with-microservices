package com.me.microservices.core.review.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.me.microservices.core.review.bo.ReviewEntity;
import com.me.microservices.core.review.repository.ReactiveReviewRepository;
import com.me.microservices.core.review.service.AsciiArtService;

@Ignore
@AutoConfigureTestDatabase(connection=EmbeddedDatabaseConnection.HSQL)
@Commit
@DataJpaTest
@Transactional
@RunWith(SpringRunner.class)
public class ReactiveReviewRepositoryDeleteTest {

	@Autowired
	ReactiveReviewRepository repo;
	
	@Autowired
	private AsciiArtService asciiArt;
	
	private static final String SUBJECT = "Washing machine";
	private static final String CONTENT = "Good product. The installation is simply.";
	private static final String AUTHOR = "rudysaniez";
	
	private static final Integer MAX_REVIEW_IN_SETUP_STEP = 100;
	
	@Before
	public void setup() {
		
		asciiArt.display("SETUP");
		
		repo.deleteAllEntities().block();
		
		IntStream.rangeClosed(1, MAX_REVIEW_IN_SETUP_STEP).
			mapToObj(id -> new ReviewEntity(id, 1, AUTHOR + id, SUBJECT + id, CONTENT + id)).
			forEach(review -> repo.save(review).block());
	}
	
	@Test
	public void delete() {
		
		asciiArt.display("DELETE");
		
		assertNotNull(repo.findByReviewId(1).block());
		assertTrue(repo.existsByReviewID(1).block());
		
		repo.deleteEntityByReviewID(1).block();
		
		assertNull(repo.findByReviewId(1).block());
		assertFalse(repo.existsByReviewID(1).block());
		
		repo.deleteEntityByProductID(1).block();
		assertThat(repo.findAll().collectList().block()).isEmpty();
		assertThat(repo.findByProductID(1, PageRequest.of(0, 20)).block().getTotalElements()).isEqualTo(0);
	}
}
