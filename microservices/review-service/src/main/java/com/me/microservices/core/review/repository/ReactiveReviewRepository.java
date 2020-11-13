package com.me.microservices.core.review.repository;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.me.microservices.core.review.bo.ReviewEntity;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

public class ReactiveReviewRepository extends ReactiveJpaRepository<ReviewEntity, Integer> {

	private ReviewRepository reviewRepository;
	
	@Autowired
	public ReactiveReviewRepository(ReviewRepository reviewRepository, Scheduler scheduler) {
		super(reviewRepository, scheduler);
		this.reviewRepository = reviewRepository;
	}
	
	/**
	 * @param id
	 * @return mono of {@link ReviewEntity}
	 */
	public Mono<ReviewEntity> findByReviewId(Integer id) {
		
		return Mono.just(id).publishOn(scheduler).
				transform(m -> m.map(reviewRepository::findByReviewID).map(Optional::get).onErrorResume(e -> Mono.empty())).
				subscribeOn(scheduler);
	}
	
	/**
	 * @param reviewID
	 * @return mono of {@link Boolean}
	 */
	public Mono<Boolean> existsByReviewID(Integer reviewID) {
		
		return Mono.just(reviewID).publishOn(scheduler).
				transform(m -> m.map(reviewRepository::existsByReviewID).onErrorReturn(Boolean.FALSE)).
				subscribeOn(scheduler);
	}
	
	/**
	 * @param productID
	 * @param page
	 * @return mono of page {@link ReviewEntity}
	 */
	public Mono<Page<ReviewEntity>> findByProductID(Integer productID, Pageable page) {
		
		return Mono.just(productID).publishOn(scheduler).
				transform(m -> m.map(i -> reviewRepository.findByProductID(i, page))).
				subscribeOn(scheduler);
	}
	
	/**
	 * @param reviewID
	 * @return mono of {@link Boolean}
	 */
	public Mono<Boolean> deleteEntityByReviewID(Integer reviewID) {
		
		return Mono.<Boolean>empty().publishOn(scheduler).
				concatWith( Mono.defer( () -> Mono.fromRunnable( () -> reviewRepository.deleteByReviewID(reviewID)))).
				single(Boolean.TRUE);
	}
	
	/**
	 * @param productID
	 * @return mono of {@link Boolean}
	 */
	public Mono<Boolean> deleteEntityByProductID(Integer productID) {
		
		return Mono.<Boolean>empty().publishOn(scheduler).
				concatWith( Mono.defer( () -> Mono.fromRunnable( () -> reviewRepository.deleteByProductID(productID)))).
				single(Boolean.TRUE);
	}
}
