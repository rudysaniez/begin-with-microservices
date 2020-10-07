package com.me.work.example.microservices.core.review.service;

import java.time.LocalDateTime;

import org.hibernate.HibernateException;
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
import com.me.work.example.handler.exception.InvalidInputException;
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
		
		if(reviewID < 1) throw new InvalidInputException("ReviewID should be greater than 0");
		
		ReviewEntity reviewEntity = reviewRepository.findByReviewID(reviewID).
				orElseThrow(() -> new NotFoundException(String.format("The review with reviewID=%d doesn't not exists.", reviewID)));
		
		log.debug("Review with id={} has been found.", reviewID);
			
		return ResponseEntity.ok(mapper.toModel(reviewEntity));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<Paged<Review>> getReviewByProductId(Integer productID, Integer pageNumber, Integer pageSize) {
		
		if(pageNumber == null) pageNumber = pagination.getDefaultPageNumber();
		if(pageSize == null) pageSize = pagination.getDefaultPageSize();
		
		if(productID < 1) throw new InvalidInputException("ProductId should be greater than 0");
		if(pageNumber < 0) throw new InvalidInputException("Page number should be greater or equal than 0");
		if(pageSize < 1) throw new InvalidInputException("Page size should be greater than 0");
		
		Page<ReviewEntity> pageOfReviewEntity = reviewRepository.findByProductID(productID, PageRequest.of(pageNumber, pageSize));
		
		log.debug("{} reviews found by productID={}.", pageOfReviewEntity.getTotalElements(), productID);
			
		return ResponseEntity.ok(toPaged(pageOfReviewEntity));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<Review> save(Review review) {
		
		try {
			
			ReviewEntity reviewEntity = mapper.toEntity(review);
			reviewEntity.setCreationDate(LocalDateTime.now());
			reviewEntity.setUpdateDate(null);
			
			reviewEntity = reviewRepository.save(reviewEntity);
			
			log.debug("This review has been saved : {}.", mapper.toModel(reviewEntity));
			
			return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toModel(reviewEntity));
		}
		catch(HibernateException e) {
			throw new InvalidInputException(String.format("Duplicate key : check the reviewID (%d).", review.getReviewID()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<Review> update(Review review, Integer reviewID) {
		
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
			
			return ResponseEntity.ok(mapper.toModel(reviewEntity));
		}
		catch(HibernateException e) {
			throw new InvalidInputException(String.format("Duplicate key : check the reviewID (%d).", review.getReviewID()));
		}
	}

	@ResponseStatus(value=HttpStatus.OK)
	@Override
	public void delete(Integer reviewID) {
	
		ReviewEntity reviewEntity = reviewRepository.findByReviewID(reviewID).
				orElseThrow(() -> new NotFoundException(String.format("The review with reviewID=%d doesn't not exists.", reviewID)));
		
		log.debug("This review with reviewID={} has been deleted : {}.", reviewID, mapper.toModel(reviewEntity).toString());
		
		reviewRepository.delete(reviewEntity);
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
