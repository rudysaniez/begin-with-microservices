package com.me.microservices.core.review.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.mapstruct.factory.Mappers;
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
import com.me.microservices.core.review.repository.ReviewRepository;

import reactor.core.publisher.Mono;

@RestController
public class ReviewServiceImpl implements ReviewsApi {
	
	private final ReviewRepository reviewRepository;
	private final ReviewMapper mapper = Mappers.getMapper(ReviewMapper.class);
	private final PaginationInformation pagination;
	
	public ReviewServiceImpl(ReviewRepository reviewRepository, PaginationInformation pagination) {
		
		this.reviewRepository = reviewRepository;
		this.pagination = pagination;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<Review>> getReview(Integer reviewID, ServerWebExchange exchange) {
		
		if(reviewID < 1) throw new InvalidInputException("ReviewID should be greater than 0.");
		
		return reviewRepository.findByReviewID(reviewID).
			switchIfEmpty(Mono.error(new NotFoundException(String.format("Review with reviewID=%d doesn't not exists.", reviewID)))).
			map(mapper::toModel).map(r -> ResponseEntity.ok(r)).
			log();
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
		Integer pSize = pageSize;
		
		return reviewRepository.countByProductID(productID).
				log().
				flatMap(count -> reviewRepository.findByProductID(productID, page).
						map(mapper::toModel).
						collectList().
						map(list -> {
							
							PagedReview pageRecommendation = new PagedReview();
							pageRecommendation.setContent(list);
							
							PageMetadata pageMetadata = new PageMetadata();
							pageMetadata.setTotalElements(count);
							pageMetadata.setSize(Integer.toUnsignedLong(page.getPageSize()));
							pageMetadata.setNumber(Integer.toUnsignedLong(page.getPageNumber()));
							pageMetadata.setTotalPages(count < pSize ? 1 : count % pSize == 0 ? count/pSize : ((count/pSize) + 1));
							pageRecommendation.setPage(pageMetadata);
	
							return ResponseEntity.ok(pageRecommendation);
						})
				).log();
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
		
		return reviewRepository.findByReviewID(reviewID).
			switchIfEmpty(Mono.error(new NotFoundException(String.format("Review with reviewID=%d doesn't not exists.", reviewID)))).
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
			flatMap(re -> reviewRepository.save(re).
					onErrorMap(DataIntegrityViolationException.class, e -> new InvalidInputException(String.format("Duplicate key : check the reviewID (%d).", re.getReviewID())))
			).
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
		
		return reviewRepository.deleteByProductID(productID).
				 map(v -> ResponseEntity.ok(v)).
				 log();
	}
}
