package com.me.microservices.core.review.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.me.api.core.common.PageMetadata;
import com.me.api.core.common.Paged;
import com.me.api.core.review.Review;
import com.me.api.core.review.ReviewService;
import com.me.handler.exception.InvalidInputException;
import com.me.handler.exception.NotFoundException;
import com.me.microservices.core.review.Application.PaginationInformation;
import com.me.microservices.core.review.bo.ReviewEntity;
import com.me.microservices.core.review.mapper.ReviewMapper;
import com.me.microservices.core.review.repository.ReviewRepository;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Slf4j
@RestController
public class ReviewServiceImpl implements ReviewService {
	
	private final ReviewRepository reviewRepository;
	private final ReviewMapper mapper;
	private final PaginationInformation pagination;
	private final Scheduler scheduler;
	
	@Autowired
	public ReviewServiceImpl(ReviewRepository reviewRepository, ReviewMapper mapper, 
			PaginationInformation pagination, Scheduler scheduler) {
		
		this.reviewRepository = reviewRepository;
		this.mapper = mapper;
		this.pagination = pagination;
		this.scheduler = scheduler;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Review> getReview(Integer reviewID) {
		
		if(reviewID < 1) throw new InvalidInputException("ReviewID should be greater than 0");
		
		Mono<Review> monoOfReview = Mono.just(reviewRepository.findByReviewID(reviewID)).
			map(opt -> opt.get()).
			switchIfEmpty(Mono.error(new NotFoundException())).
			log().
			map(mapper::toModel);
		
		return Mono.defer(() -> monoOfReview).subscribeOn(scheduler);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Paged<Review>> getReviewByProductId(Integer productID, Integer pageNumber, Integer pageSize) {
		
		if(productID < 1) throw new InvalidInputException("ProductId should be greater than 0");
		if(pageNumber == null || pageNumber < 0) pageNumber = pagination.getDefaultPageNumber();
		if(pageSize == null || pageSize < 1) pageSize = pagination.getDefaultPageSize();
		
		Mono<Paged<Review>> monoOfPaged = Mono.just(reviewRepository.findByProductID(productID, PageRequest.of(pageNumber, pageSize))).
			map(page -> toPaged(page));
		
		return Mono.defer(() -> monoOfPaged).subscribeOn(scheduler);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Review> save(Review review) {
		
		try {
			
			ReviewEntity reviewEntity = mapper.toEntity(review);
			reviewEntity.setCreationDate(LocalDateTime.now());
			reviewEntity.setUpdateDate(null);
			
			reviewEntity = reviewRepository.save(reviewEntity);
			
			log.debug("This review has been saved : {}.", mapper.toModel(reviewEntity));
			
			return Mono.empty();
		}
		catch(DataIntegrityViolationException e) {
			throw new InvalidInputException(String.format("Duplicate key : check the reviewID (%d).", review.getReviewID()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Review> update(Review review, Integer reviewID) {
		
		try {
			
			if(reviewID < 1) throw new InvalidInputException("ReviewID should be greater than 0");
			
			ReviewEntity reviewEntity = reviewRepository.findByReviewID(reviewID).
					orElseThrow(() -> new NotFoundException(String.format("The review with reviewID=%d doesn't not exists.", reviewID)));
				
			reviewEntity.setAuthor(review.getAuthor());
			reviewEntity.setContent(review.getContent());
			reviewEntity.setSubject(review.getSubject());
			reviewEntity.setUpdateDate(LocalDateTime.now());
			
			reviewEntity = reviewRepository.save(reviewEntity);
			
			log.debug("This review with reviewID={} has been updated : {}.", reviewID, mapper.toModel(reviewEntity));
			
			return Mono.empty();
		}
		catch(DataIntegrityViolationException e) {
			throw new InvalidInputException(String.format("Duplicate key : check the reviewID (%d).", review.getReviewID()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@ResponseStatus(value=HttpStatus.OK)
	@Override
	public Mono<Void> deleteReview(Integer reviewID) {
	
		ReviewEntity reviewEntity = reviewRepository.findByReviewID(reviewID).
				orElseThrow(() -> new NotFoundException(String.format("The review with reviewID=%d doesn't not exists.", reviewID)));
		
		log.debug("This review with reviewID={} has been deleted : {}.", reviewID, mapper.toModel(reviewEntity).toString());
		
		return Mono.empty();
	}

	/**
	 * @param pageOfReviewEntity
	 * @return paged of {@link Review}
	 */
	private Paged<Review> toPaged(Page<ReviewEntity> pageOfReviewEntity) {
		
		Page<Review> pageOfReviewModel = pageOfReviewEntity.map(r -> mapper.toModel(r));
		
		PageMetadata metadata = new PageMetadata(pageOfReviewModel.getSize(), pageOfReviewModel.getTotalElements(), 
				pageOfReviewModel.getTotalPages(), pageOfReviewModel.getNumber());
		
		return new Paged<>(pageOfReviewModel.getContent(), metadata);
	}
}
