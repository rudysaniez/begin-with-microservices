package com.me.work.example.microservices.core.review.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.me.work.example.api.core.common.PageMetadata;
import com.me.work.example.api.core.common.Paged;
import com.me.work.example.api.core.review.Review;
import com.me.work.example.api.core.review.ReviewService;
import com.me.work.example.handler.exception.AlreadyExistsException;
import com.me.work.example.handler.exception.NotFoundException;
import com.me.work.example.microservices.core.review.Application.PaginationInformation;
import com.me.work.example.microservices.core.review.bo.ReviewEntity;
import com.me.work.example.microservices.core.review.mapper.ReviewMapper;
import com.me.work.example.microservices.core.review.repository.ReviewRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ReviewServiceImpl implements ReviewService {
	
	private final ReviewRepository reviewRepository;
	private final ReviewMapper mapper;
	private final PaginationInformation pagination;
	
	@Autowired
	public ReviewServiceImpl(ReviewRepository reviewRepository, ReviewMapper mapper, 
			PaginationInformation pagination) {
		
		this.reviewRepository = reviewRepository;
		this.mapper = mapper;
		this.pagination = pagination;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<Review> getReview(Integer reviewID) {
		
		Optional<ReviewEntity> optionalOfReviewEntity = reviewRepository.findByReviewID(reviewID);
		
		if(optionalOfReviewEntity.isPresent()) {
			
			if(log.isInfoEnabled()) 
				log.info(" > Review with id={} has been found.", optionalOfReviewEntity.get().getReviewID());
			
			return ResponseEntity.ok(mapper.toModel(optionalOfReviewEntity.get()));
		}
		
		if(log.isDebugEnabled()) log.debug("Review with id={} doesn't not exists.", reviewID);
		
		throw new NotFoundException(String.format("The review with id=%d doesn't not exists.", reviewID));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<Paged<Review>> getReviewByProductId(Integer productId, Integer pageNumber, Integer pageSize) {
		
		if(pageNumber == null) pageNumber = pagination.getDefaultPageNumber();
		if(pageSize == null) pageSize = pagination.getDefaultPageSize();
		
		Page<ReviewEntity> pageOfReviewEntity = reviewRepository.findByProductID(productId, PageRequest.of(pageNumber, pageSize));
		
		if(!pageOfReviewEntity.isEmpty()) {
			
			if(log.isInfoEnabled()) 
				log.info(" > {} reviews has been found.", pageOfReviewEntity.getContent().size());
			
			return ResponseEntity.ok(toPaged(pageOfReviewEntity));
		}
		
		if(log.isDebugEnabled()) log.debug("Reviews with productId={} doesn't not exists.", productId);
		
		throw new NotFoundException(String.format("The review with productId=%d doesn't not exists.", productId));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<Review> save(Review review) {
		
		Optional<ReviewEntity> optOfReviewEntity = reviewRepository.findByReviewIDAndProductID(review.getReviewID(), 
				review.getProductID());
		
		if(optOfReviewEntity.isEmpty()) {
			
			ReviewEntity toSaved = mapper.toEntity(review);
			toSaved.setId(null);
			toSaved.setCreationDate(LocalDateTime.now());
			toSaved.setUpdateDate(null);
			
			toSaved = reviewRepository.save(toSaved);
			
			return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toModel(toSaved));
		}
		
		if(log.isDebugEnabled()) log.debug("Review already exists. Use Verb PUT with the identifier like path variable.");
		
		throw new AlreadyExistsException(String.format("This review already exists : %s. "
				+ "Use Verb PUT with the identifier like path variable.", 
					optOfReviewEntity.get().toString()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<Review> update(Review review, Integer reviewID) {
		
		Optional<ReviewEntity> optOfReview = reviewRepository.findByReviewID(reviewID);
		
		if(optOfReview.isPresent()) {
			
			ReviewEntity toUpdated = optOfReview.get();
			toUpdated.setAuthor(review.getAuthor());
			toUpdated.setContent(review.getContent());
			toUpdated.setSubject(review.getSubject());
			toUpdated.setUpdateDate(LocalDateTime.now());
			
			toUpdated = reviewRepository.save(toUpdated);
			
			return ResponseEntity.ok(mapper.toModel(toUpdated));
		}
		
		if(log.isDebugEnabled()) log.debug("Review with id={} doesn't not exists.", reviewID);
		
		throw new NotFoundException(String.format("The review with id=%d doesn't not exists. "
				+ "Use Verb POST for created this review.", reviewID));
	}

	@ResponseStatus(value=HttpStatus.OK)
	@Override
	public void delete(Integer reviewID) {
	
		Optional<ReviewEntity> optOfReview = reviewRepository.findByReviewID(reviewID);
		
		if(optOfReview.isPresent())
			reviewRepository.delete(optOfReview.get());
		else {
			
			if(log.isDebugEnabled()) log.debug("Review with id={} doesn't not exists.", reviewID);
			throw new NotFoundException(String.format("Review with id=%d doesn't not exists.", reviewID));
		}
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
