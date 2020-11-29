package com.me.microservices.core.review.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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
import com.me.microservices.core.review.repository.ReactiveReviewRepository;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class ReviewServiceImpl implements ReviewService {
	
	private final ReactiveReviewRepository reviewRepository;
	private final ReviewMapper mapper;
	private final PaginationInformation pagination;
	
	@Autowired
	public ReviewServiceImpl(ReactiveReviewRepository reviewRepository, ReviewMapper mapper, 
			PaginationInformation pagination) {
		
		this.reviewRepository = reviewRepository;
		this.mapper = mapper;
		this.pagination = pagination;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@ResponseStatus(value=HttpStatus.OK)
	@Override
	public Mono<Review> getReview(Integer reviewID) {
		
		if(reviewID < 1) throw new InvalidInputException("ReviewID should be greater than 0.");
		
		log.info(" > Find Review by reviewID={}.", reviewID);
		
		return reviewRepository.findByReviewId(reviewID).
				switchIfEmpty(Mono.error(new NotFoundException(String.format("Review with reviewID=%d doesn't not exists.", reviewID)))).
				log().
				map(mapper::toModel);
	}

	/**
	 * {@inheritDoc}
	 */
	@ResponseStatus(value=HttpStatus.OK)
	@Override
	public Mono<Paged<Review>> getReviewByProductId(Integer productID, Integer pageNumber, Integer pageSize) {
		
		if(productID < 1) throw new InvalidInputException("ProductId should be greater than 0.");
		if(pageNumber == null || pageNumber < 0) pageNumber = pagination.getDefaultPageNumber();
		if(pageSize == null || pageSize < 1) pageSize = pagination.getDefaultPageSize();
		
		log.info(" > Find review by productID={}, pageNumber={} and pageSize={}.", productID, pageNumber, pageSize);
		
		return reviewRepository.findByProductID(productID, PageRequest.of(pageNumber, pageSize, Sort.by(Direction.ASC, "reviewID"))).
				log().
				transform(monoOfPage -> monoOfPage.map(pageOfEntity -> toPaged(pageOfEntity)));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@ResponseStatus(value=HttpStatus.CREATED)
	@Override
	public Mono<Review> save(Review review) {
		
		if(review.getReviewID() < 1) throw new InvalidInputException("ReviewID should be greater than 0.");
		
		log.info(" > Save the {}", review.toString());
		
		ReviewEntity reviewEntity = mapper.toEntity(review);
		reviewEntity.setCreationDate(LocalDateTime.now());
		reviewEntity.setUpdateDate(null);
		
		return reviewRepository.save(reviewEntity).
				onErrorMap(DataIntegrityViolationException.class, 
						e -> new InvalidInputException(String.format("Duplicate key : check the reviewID (%d).", review.getReviewID()))).
				log().
				map(mapper::toModel);
	}

	/**
	 * {@inheritDoc}
	 */
	@ResponseStatus(value=HttpStatus.OK)
	@Override
	public Mono<Review> update(Review review, Integer reviewID) {
		
		if(reviewID < 1) throw new InvalidInputException("ReviewID should be greater than 0.");
		if(review.getProductID() < 1) throw new InvalidInputException("ProductID in Review should be greater than 0.");
		
		return reviewRepository.findByReviewId(reviewID).
				switchIfEmpty(Mono.error(new NotFoundException(String.format("Review with reviewID=%d doesn't not exists.", reviewID)))).
				log().
				transform(m -> m.map(reviewEntity -> {
					
					reviewEntity.setAuthor(review.getAuthor());
					reviewEntity.setContent(review.getContent());
					reviewEntity.setProductID(review.getProductID());
					reviewEntity.setSubject(review.getSubject());
					reviewEntity.setUpdateDate(LocalDateTime.now());
					return reviewEntity;
				}).flatMap(reviewRepository::save).
						onErrorMap(DataIntegrityViolationException.class, 
								e -> new InvalidInputException(String.format("Duplicate key : check the reviewID (%d).", 
										review.getReviewID()))).
						log()).
				map(mapper::toModel);
	}

	/**
	 * {@inheritDoc}
	 */
	@ResponseStatus(value=HttpStatus.OK)
	@Override
	public Mono<Void> deleteReviews(Integer productID) {
	
		if(productID < 1) throw new InvalidInputException("ProductID should be greater than 0.");
		
		return reviewRepository.deleteEntityByProductID(productID).flatMap(b -> Mono.<Void>empty());
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
