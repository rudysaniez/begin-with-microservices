package com.me.microservices.core.review.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.me.microservices.core.review.bo.ReviewEntity;
import com.me.microservices.core.review.repository.ReactiveReviewRepository;
import com.me.microservices.core.review.service.AsciiArtService;

@Ignore
@AutoConfigureTestDatabase(connection=EmbeddedDatabaseConnection.HSQL)
@DataJpaTest
@Commit
@Transactional
@RunWith(SpringRunner.class)
public class ReactiveReviewRepositorySaveTest {

	@Autowired
	ReactiveReviewRepository repo;
	
	@Autowired
	private AsciiArtService asciiArt;
	
	private static final String SUBJECT = "Washing machine";
	private static final String CONTENT = "Good product. The installation is simply.";
	private static final String AUTHOR = "rudysaniez";
	
	private static final Integer MAX_REVIEW_IN_SETUP_STEP = 30;
	
	@Before
	public void setup() {
	
		asciiArt.display("SETUP");
		
		IntStream.rangeClosed(1, MAX_REVIEW_IN_SETUP_STEP).
			mapToObj(id -> new ReviewEntity(id, 1, AUTHOR + id, SUBJECT + id, CONTENT + id)).
			forEach(review -> repo.save(review).block());
		
	}
	
	@Test
	public void find() {
		
		asciiArt.display("SAVE  OTHER  REVIEW_ENTITY  AND  LAUNCH  SOMES  FIND  METHODS");
		
		Random random =  new Random();
		
		/**
		 * Save all from iterable.
		 */
		repo.saveAll(	
			IntStream.rangeClosed(MAX_REVIEW_IN_SETUP_STEP + 1, MAX_REVIEW_IN_SETUP_STEP + random.nextInt(30)).
				mapToObj(id -> new ReviewEntity(id, 2, AUTHOR + id, SUBJECT + id, CONTENT + id)).
				collect(Collectors.toList())
			).
			collectList().block();
		
		
		List<ReviewEntity> allReviews = repo.findAll().collectList().block();
		assertThat(allReviews).isNotEmpty();
		assertThat(repo.count().block()).isEqualTo(allReviews.size());
		
		/**
		 * Find by productID.
		 */
		Page<ReviewEntity> pageOfReview = repo.findByProductID(1, PageRequest.of(0, 10, Sort.by(Direction.ASC, "reviewID"))).block();
		assertThat(pageOfReview.getContent()).size().isEqualTo(10);
		assertThat(pageOfReview.getTotalElements()).isEqualTo(30);
		assertThat(pageOfReview.getNumberOfElements()).isEqualTo(10);
		assertThat(pageOfReview.getTotalPages()).isEqualTo(3);
		
		/**
		 * Find all by IDs.
		 */
		List<ReviewEntity> someReviews = repo.findAllById(
					pageOfReview.getContent().stream().map(r -> r.getId()).distinct().limit(5).collect(Collectors.toList())
			).collectList().block();
		
		assertThat(someReviews).size().isEqualTo(5);
		
		/**
		 * Find by ID and by reviewID.
		 */
		ReviewEntity oneReviewByID = repo.findById(someReviews.stream().findFirst().get().getId()).block();
		ReviewEntity oneReviewByReviewID = repo.findByReviewId(someReviews.stream().findFirst().get().getReviewID()).block();
		assertEquals(oneReviewByID, oneReviewByReviewID);
	}
}
