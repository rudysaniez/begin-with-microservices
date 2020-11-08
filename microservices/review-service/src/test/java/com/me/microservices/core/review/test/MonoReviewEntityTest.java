package com.me.microservices.core.review.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.me.handler.exception.InvalidInputException;
import com.me.handler.exception.NotFoundException;
import com.me.microservices.core.review.bo.ReviewEntity;
import com.me.microservices.core.review.repository.ReviewRepository;
import com.me.microservices.core.review.service.AsciiArtService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@DataJpaTest
@AutoConfigureTestDatabase(connection=EmbeddedDatabaseConnection.HSQL)
@Transactional
@Commit
@RunWith(SpringRunner.class)
public class MonoReviewEntityTest {

	@Autowired
	private Scheduler scheduler;
	
	@Autowired
	private ReviewRepository reviewRepository;
	
	@Autowired
	private AsciiArtService asciiArt;
	
	private static ReviewEntity savedReview;
	
	private static final Integer REVIEW_ID = 999;
	private static final Integer PRODUCT_ID = 999;
	private static final String SUBJECT = "Washing machine";
	private static final String CONTENT = "Good product. The installation is simply.";
	private static final String AUTHOR = "rudysaniez";
	
	@Before
	public void setupdb() {
		
		asciiArt.display("SETUP");
		
		/**
		 * DeleteAll.
		 */
		deleteAll();
		
		List<ReviewEntity> reviews = findAll();
		assertThat(reviews).isEmpty();
		
		if(savedReview != null)
			assertFalse(exists(savedReview.getId()));
		
		/**
		 * Create.
		 */
		ReviewEntity reviewEntity = new ReviewEntity(REVIEW_ID, PRODUCT_ID, AUTHOR, SUBJECT, CONTENT);
		
		savedReview = save(reviewEntity);
		assertNotNull(savedReview);
		
		/**
		 * findAll.
		 */
		reviews = findAll();
		assertThat(reviews).size().isEqualTo(1);
		
		/**
		 * FindById.
		 */
		reviewEntity = findById(savedReview.getId());
		assertEqualsReview(reviewEntity, savedReview);
		
		/**
		 * Exists with Mono of ID.
		 */
		boolean exists = exists(Mono.just(savedReview.getId()));
		assertEquals(true, exists);
	}
	
	public void getReview() {
		
		asciiArt.display("GET  REVIEW");
		
		/**
		 * Get by reviewID.
		 */
		ReviewEntity entity = findByReviewId(REVIEW_ID);
		assertNotNull(entity);
		
		/**
		 * Create.
		 */
		entity = new ReviewEntity(1, 1, "rsaniez", "Good job", "This phone is excellent!");
		entity = save(entity);
		assertNotNull(entity);
		
		/**
		 * Find by ID.
		 */
		entity = findById(entity.getId());
		assertNotNull(entity);
		
		/**
		 * Delete by ID.
		 */
		deleteById(entity.getId());
		
		/**
		 * Create.
		 */
		entity = new ReviewEntity(2, 1, "rsaniez", "Good job!", "This phone is very excellent!");
		entity = save(Mono.just(entity));
		assertNotNull(entity); assertEquals(2, entity.getReviewID());
		
		/**
		 * Delete by ID.
		 */
		deleteById(entity.getId());
		deleteById(savedReview.getId());
		
		List<ReviewEntity> reviews = findAll();
		assertThat(reviews).isEmpty();
	}

	public void createReview() {
		
		asciiArt.display("SAVE  REVIEW");
		
		ReviewEntity entity = new ReviewEntity(3, 3, "rsaniez", "Good job", "This phone is very very excellent!");
		
		final ReviewEntity saveEntity = save(Mono.just(entity));
		assertNotNull(saveEntity);
		
		entity = findByReviewId(saveEntity.getReviewID());
		assertNotNull(entity);
	}
	
	public void update() {
		
		asciiArt.display("UPDATE  REVIEW");
		
		ReviewEntity entityUpdated = findByReviewId(REVIEW_ID);
		entityUpdated.setContent("This machine is very very well!");
		entityUpdated = save(Mono.just(entityUpdated));
		
		assertEquals("This machine is very very well!", entityUpdated.getContent());
		
		ReviewEntity entityFound = findById(entityUpdated.getId());
		assertEquals("This machine is very very well!", entityFound.getContent());
		
		assertEqualsReview(entityFound, entityUpdated);
	}
	
	@Test(expected=NotFoundException.class)
	public void findNotFoundException() {
		
		asciiArt.display("NOT  FOUND");
		
		ReviewEntity entity = findByReviewId(REVIEW_ID);
		assertNotNull(entity);
		
		deleteById(entity.getId());
		
		assertFalse(exists(entity.getId()));
		
		findByReviewId(REVIEW_ID);
	}
	
	public void saveAll() {
		
		asciiArt.display("SAVE  ALL");
		
		List<ReviewEntity> reviews = save(List.of(new ReviewEntity(1, PRODUCT_ID, AUTHOR, SUBJECT, CONTENT), 
				new ReviewEntity(2, 1, AUTHOR, SUBJECT, CONTENT)));
		
		assertThat(reviews).size().isEqualTo(2);
	}
	
	//@Test(expected=InvalidInputException.class)
	public void DataIntegrityViolationException() {
		
		asciiArt.display("DATA  INTEGRITY  VIOLATION  EXCEPTION");
		
		save(new ReviewEntity(REVIEW_ID, PRODUCT_ID, AUTHOR, SUBJECT, CONTENT));
	}
	
	public void findAllByIDs() {
		
		List<ReviewEntity> reviews = save(List.of(new ReviewEntity(2, 1, AUTHOR + "_2", SUBJECT + "_2", CONTENT + "_2"),
				new ReviewEntity(3, 1, AUTHOR + "_3", SUBJECT + "_3", CONTENT + "_3")));
		
		assertThat(findAll(reviews.stream().map(r -> r.getId()).collect(Collectors.toList()))).size().isEqualTo(2);
	}
	
	
	
	/**
	 * CORE.
	 */
	
	
	/**
	 * @param entity
	 * @return {@link ReviewEntity}
	 */
	private ReviewEntity save(ReviewEntity entity) {
		
		return Mono.just(entity).flux().publishOn(scheduler).
				transform(flux -> flux.map(reviewRepository::save)).
					onErrorMap(DataIntegrityViolationException.class, e -> new InvalidInputException()).
				subscribeOn(scheduler).publish().autoConnect(0).
				single().block();
	}
	
	/**
	 * @param entities
	 * @return list of {@link ReviewEntity}
	 */
	private List<ReviewEntity> save(Iterable<ReviewEntity> entities) {
		
		return Mono.just(entities).flux().publishOn(scheduler).
			transform(flux -> flux.map(reviewRepository::saveAll).
				onErrorMap(DataIntegrityViolationException.class, e -> new InvalidInputException())).
					flatMap(Flux::fromIterable).
			subscribeOn(scheduler).publish().autoConnect(0).
			collectList().block();
	}
	
	/**
	 * @param entityStream
	 * @return {@link ReviewEntity}
	 */
	private ReviewEntity save(Publisher<ReviewEntity> entityStream) {
		
		return Mono.from(entityStream).flux().publishOn(scheduler).
			transform(flux -> flux.map(reviewRepository::save).
					onErrorMap(DataIntegrityViolationException.class, e -> new InvalidInputException()).log()).
			subscribeOn(scheduler).publish().autoConnect(0).
			single().block();
	}
	
	/**
	 * @param id
	 * @return {@link ReviewEntity}
	 */
	private ReviewEntity findById(Integer id) {
		
		return Mono.just(id).flux().publishOn(scheduler).
				transform(flux -> flux.map(reviewRepository::findById).map(Optional::get).
						switchIfEmpty(Mono.error(new NotFoundException())).log()).
				subscribeOn(scheduler).publish().autoConnect(0).
				single().block();
	}
	
	/**
	 * @param id
	 * @return {@link ReviewEntity}
	 */
	private ReviewEntity findByReviewId(Integer id) {
		
		 return Mono.just(id).flux().publishOn(scheduler).
				transform(flux  -> flux.map(reviewRepository::findByReviewID).
						map(opt -> opt.orElseThrow(() -> new NotFoundException("The review with id="+id+" doesn't not exists."))).log()).
				subscribeOn(scheduler).publish().autoConnect(0).
				single().block();
	}
	
	/**
	 * @return list of {@link ReviewEntity}
	 */
	private List<ReviewEntity> findAll() {
		
		return Mono.<ReviewEntity>empty().flux().publishOn(scheduler).
			concatWith( Flux.defer(() -> Flux.fromIterable(reviewRepository.findAll()))).
			publishOn(scheduler).publish().autoConnect(0).
			collectList().block();
	}

	/**
	 * @param ids
	 * @return list of {@link ReviewEntity}
	 */
	private List<ReviewEntity> findAll(Iterable<Integer> ids) {
		
		return Mono.just(ids).flux().publishOn(scheduler).
				transform(flux -> Flux.fromIterable( reviewRepository.findAllById(ids))).
				subscribeOn(scheduler).publish().autoConnect(0).
				collectList().block();
	}
	
	/**
 	 * @param ID
	 * @return True or False
	 */
	private boolean exists(Integer ID) {
		
		return Mono.just(ID).flux().publishOn(scheduler).
				transform(flux -> flux.map(reviewRepository::existsById).onErrorReturn(false)).
				subscribeOn(scheduler).publish().autoConnect(0).
				single().block();
	}
	
	/**
	 * @param ID
	 * @return True or False
	 */
	private boolean exists(Mono<Integer> ID) {
		
		return ID.flux().publishOn(scheduler).
				transform(flux -> flux.map(reviewRepository::existsById).onErrorReturn(false)).
				subscribeOn(scheduler).publish().autoConnect(0).
				single().block();
	}
	
	/**
	 * @param ID
	 */
	private void deleteById(Integer ID) {
		
		Mono.<Boolean>empty().publishOn(scheduler).
			concatWith(Mono.<Boolean>defer(() -> Mono.fromRunnable( () -> reviewRepository.deleteById(ID)))).
				single(Boolean.TRUE).block();
	}
	
	private void deleteAll() {
		
		Mono.<Boolean>empty().publishOn(scheduler).
			concatWith( Mono.defer( () -> Mono.fromRunnable( () -> reviewRepository.deleteAll()))).
				single(Boolean.TRUE).block();
	}
	
	private <T> Mono<T> asyncMono(Supplier<Mono<T>> supplier) {
		return Mono.defer(supplier).subscribeOn(scheduler);
	}
	
	public void assertEqualsReview(ReviewEntity expectedReview, ReviewEntity actualReview) {
		
		assertEquals(expectedReview.getContent(), actualReview.getContent());
		assertEquals(expectedReview.getAuthor(), actualReview.getAuthor());
		assertEquals(expectedReview.getProductID(), actualReview.getProductID());
		assertEquals(expectedReview.getReviewID(), actualReview.getReviewID());
		assertEquals(expectedReview.getSubject(), actualReview.getSubject());
	}
}
