package com.me.microservices.core.review.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.me.handler.exception.InvalidInputException;
import com.me.handler.exception.NotFoundException;
import com.me.microservices.core.review.Application.PaginationInformation;
import com.me.microservices.core.review.api.ReviewsApi;
import com.me.microservices.core.review.api.model.PageMetadata;
import com.me.microservices.core.review.api.model.PagedReview;
import com.me.microservices.core.review.api.model.Review;
import com.me.microservices.core.review.bo.ReviewEntity;
import com.me.microservices.core.review.mapper.ReviewMapper;
import com.me.microservices.core.review.repository.ReactiveReviewRepository;

import reactor.core.publisher.Mono;

@RestController
public class ReviewServiceImpl implements ReviewsApi {
	
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
	@Override
	public Mono<ResponseEntity<Review>> getReview(Integer reviewID, ServerWebExchange exchange) {
		
		if(reviewID < 1) throw new InvalidInputException("ReviewID should be greater than 0.");
		
		return reviewRepository.findByReviewId(reviewID).
			switchIfEmpty(Mono.error(new NotFoundException(String.format("Review with reviewID=%d doesn't not exists.", reviewID)))).
			log().
			map(mapper::toModel).map(r -> ResponseEntity.ok(r));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<PagedReview>> getReviewByProductId(Integer productID, Integer pageNumber, Integer pageSize, ServerWebExchange exchange) {
		
		if(productID < 1) throw new InvalidInputException("ProductId should be greater than 0.");
		if(pageNumber == null || pageNumber < 0) pageNumber = pagination.getDefaultPageNumber();
		if(pageSize == null || pageSize < 1) pageSize = pagination.getDefaultPageSize();
		
		Pageable page = PageRequest.of(pageNumber, pageSize, Sort.by(Direction.ASC, "reviewID"));
		
		return reviewRepository.findByProductID(productID, page).
			log().
			map(pageOfReview -> {
				
				PagedReview pagedReview = new PagedReview();
				
				PageMetadata pageMetadata = new PageMetadata();
				pageMetadata.setNumber(Integer.toUnsignedLong(pageOfReview.getNumber()));
				pageMetadata.setSize(Integer.toUnsignedLong(pageOfReview.getSize()));
				pageMetadata.setTotalElements(pageOfReview.getTotalElements());
				pageMetadata.setTotalPages(Integer.toUnsignedLong(pageOfReview.getTotalPages()));
				
				pagedReview.setPage(pageMetadata);
				pagedReview.setContent(mapper.toListModel(pageOfReview.getContent()));
				
				return pagedReview;
			}).
			map(pagedReview -> ResponseEntity.ok(pagedReview)).
			log();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<Review>> save(Mono<Review> review, ServerWebExchange exchange) {
		
		return review.map(r -> {
			if(r.getReviewID() < 1) throw new InvalidInputException("ReviewID should be greater than 0.");
			return r;
		}).
		map(mapper::toEntity).
		map(re -> {
			re.setCreationDate(LocalDateTime.now());
			return re;
		}).
		flatMap(re -> reviewRepository.save(re).
				onErrorMap(DataIntegrityViolationException.class, e -> new InvalidInputException(String.format("Duplicate key : check the reviewID (%d).", re.getReviewID())))).
		log().
		map(mapper::toModel).
		map(r -> ResponseEntity.status(HttpStatus.CREATED).body(r)).
		log();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<Review>> update(Integer reviewID, Mono<Review> review, ServerWebExchange exchange) {
		
		if(reviewID < 1) throw new InvalidInputException("ReviewID should be greater than 0.");
		
		Mono<ReviewEntity> modelToEntity = review.map(r -> {
			
			if(r.getProductID() < 1) throw new InvalidInputException("ProductID in Review should be greater than 0.");
			return r;
		}).map(mapper::toEntity);
		
		return reviewRepository.findByReviewId(reviewID).
			switchIfEmpty(Mono.error(new NotFoundException(String.format("Review with reviewID=%d doesn't not exists.", reviewID)))).
			log().
			transform(m -> m.concatWith(modelToEntity).collectList().
					map(list -> {
						
						Optional<ReviewEntity> reviewInDatabase = list.stream().filter(re -> re.getId() != null).findFirst();
						Optional<ReviewEntity> reviewModel = list.stream().filter(re -> re.getId() == null).findFirst();
						
						if(reviewInDatabase.isPresent() && reviewModel.isPresent()) {
							
							reviewInDatabase.get().setAuthor(reviewModel.get().getAuthor());
							reviewInDatabase.get().setContent(reviewModel.get().getContent());
							reviewInDatabase.get().setProductID(reviewModel.get().getProductID());
							reviewInDatabase.get().setSubject(reviewModel.get().getSubject());
							reviewInDatabase.get().setUpdateDate(LocalDateTime.now());
						}
						
						return reviewInDatabase.get();
					})
			).
			log().
			flatMap(re -> reviewRepository.save(re).
					onErrorMap(DataIntegrityViolationException.class, e -> new InvalidInputException(String.format("Duplicate key : check the reviewID (%d).", re.getReviewID())))
			).
			log().
			map(mapper::toModel).
			map(r -> ResponseEntity.ok(r)).
			log();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<Void>> deleteReviews(Integer productID, ServerWebExchange exchange) {
	
		if(productID < 1) throw new InvalidInputException("ProductID should be greater than 0.");
		
		return reviewRepository.deleteEntityByProductID(productID).
				flatMap(b -> Mono.<Void>empty()).
				map(v -> ResponseEntity.ok(v));
	}
}
