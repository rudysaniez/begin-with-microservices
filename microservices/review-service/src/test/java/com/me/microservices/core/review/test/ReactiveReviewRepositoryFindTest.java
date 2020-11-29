package com.me.microservices.core.review.test;

import static org.junit.Assert.assertTrue;

import java.util.List;
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
@Transactional
@Commit
@DataJpaTest
@RunWith(SpringRunner.class)
public class ReactiveReviewRepositoryFindTest {
	
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
		
		IntStream.rangeClosed(1, MAX_REVIEW_IN_SETUP_STEP).
			mapToObj(id -> new ReviewEntity(id, 1, AUTHOR + id, SUBJECT + id, CONTENT + id)).
			forEach(review -> repo.save(review).block());
		
		repo.saveAll(
			IntStream.rangeClosed(MAX_REVIEW_IN_SETUP_STEP + 1, MAX_REVIEW_IN_SETUP_STEP * 2).
				mapToObj(id -> new ReviewEntity(id, 2, AUTHOR + id, SUBJECT + id, CONTENT + id)).
				collect(Collectors.toList())
		).collectList().block();
	}
	
	@Test
	public void findByReviewID() {
		
		asciiArt.display("FIND BY REVIEW ID");
		
		Page<ReviewEntity> pageOfReviews = repo.findByProductID(2, PageRequest.of(0, 20, Sort.by(Direction.ASC, "reviewID"))).block();
		
		List<ReviewEntity> reviews = repo.findAllById(pageOfReviews.stream().distinct().limit(10).map(r -> r.getId()).
					collect(Collectors.toList())).
				collectList().block().
					stream().sorted( (x,y) -> x.getReviewID() < y.getReviewID() ? -1 : 1).collect(Collectors.toList());
		
		assertTrue(reviews.get(0).getReviewID().equals(pageOfReviews.get().findFirst().get().getReviewID()));
	}
}
