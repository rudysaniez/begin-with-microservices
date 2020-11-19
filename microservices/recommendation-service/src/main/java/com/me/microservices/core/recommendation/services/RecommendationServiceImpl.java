package com.me.microservices.core.recommendation.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.me.api.core.common.PageMetadata;
import com.me.api.core.common.Paged;
import com.me.api.core.recommendation.Recommendation;
import com.me.api.core.recommendation.RecommendationService;
import com.me.handler.exception.InvalidInputException;
import com.me.handler.exception.NotFoundException;
import com.me.microservices.core.recommendation.Application.PaginationInformation;
import com.me.microservices.core.recommendation.bo.RecommendationEntity;
import com.me.microservices.core.recommendation.mapper.RecommendationMapper;
import com.me.microservices.core.recommendation.repository.RecommendationRepository;

import reactor.core.publisher.Mono;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

	private final RecommendationRepository recommendationRepository;
	private final RecommendationMapper mapper;
	private final PaginationInformation pagination;
	
	/**
	 * @param recommendationRepository
	 * @param mapper
	 * @param pagination
	 */
	@Autowired
	public RecommendationServiceImpl(RecommendationRepository recommendationRepository, RecommendationMapper mapper,
			PaginationInformation pagination) {
		
		this.recommendationRepository = recommendationRepository;
		this.mapper = mapper;
		this.pagination = pagination;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@ResponseStatus(value=HttpStatus.OK)
	@Override
	public Mono<Recommendation> getRecommendation(Integer recommendationID) {
		
		if(recommendationID < 1) throw new InvalidInputException("RecommendationID should be greater than 0");
		
		return recommendationRepository.findByRecommendationID(recommendationID).
			switchIfEmpty(Mono.error(new NotFoundException(String.format("Recommendation with recommendationID=%d doesn't not exists.", 
					recommendationID)))).
			log().
			map(mapper::toModel);
	}

	/**
	 * {@inheritDoc}
	 */
	@ResponseStatus(value=HttpStatus.OK)
	@Override
	public Mono<Paged<Recommendation>> getRecommendationByProductId(Integer productID, Integer pageNumber, Integer pageSize) {

		if(productID < 1) throw new InvalidInputException("ProductId should be greater than 0");
		if(pageNumber == null || pageNumber < 0) pageNumber = pagination.getPageNumber();
		if(pageSize == null || pageSize < 1) pageSize = pagination.getPageSize();
		
		Pageable page = PageRequest.of(pageNumber, pageSize, Sort.by(Direction.ASC, "recommendationID"));
		Integer pSize = pageSize;
		
		return recommendationRepository.countByProductID(productID).
			flatMap(count -> recommendationRepository.findByProductID(productID, page).log().
					map(mapper::toModel).
					collectList().
					map(list -> new Paged<>(list, new PageMetadata(page.getPageSize(), count, count < pSize ? 1 : count % pSize == 0 ? count/pSize : ((count/pSize) + 1), page.getPageNumber()))));
	}

	/**
	 * {@inheritDoc}
	 */
	@ResponseStatus(value=HttpStatus.CREATED)
	@Override
	public Mono<Recommendation> save(Recommendation recommendation) {
		
		if(recommendation.getRecommendationID() < 1) throw new InvalidInputException("RecommendationID should be greater than 0.");
		
		RecommendationEntity recommendationEntity = mapper.toEntity(recommendation);
		recommendationEntity.setCreationDate(LocalDateTime.now());
		
		return recommendationRepository.save(recommendationEntity).
				onErrorMap(DuplicateKeyException.class, e -> new InvalidInputException(String.format("Duplicate key : check the recommendationID (%d).",
						recommendation.getRecommendationID()))).
				log().
				map(mapper::toModel);
	}

	/**
	 * {@link Recommendation}
	 */
	@ResponseStatus(value=HttpStatus.OK)
	@Override
	public Mono<Recommendation> update(Recommendation recommendation, Integer recommendationID) {
		
		if(recommendation.getRecommendationID() < 1) throw new InvalidInputException("RecommendationID should be greater than 0.");
		if(recommendation.getProductID() < 1) throw new InvalidInputException("ProductID in Recommendation should be greater than 0.");
		
		return recommendationRepository.findByRecommendationID(recommendationID).
			switchIfEmpty(Mono.error(new NotFoundException())).
			log().
			map(entity -> {
				
				entity.setAuthor(recommendation.getAuthor());
				entity.setContent(recommendation.getContent());
				entity.setProductID(recommendation.getProductID());
				entity.setRate(recommendation.getRate());
				entity.setUpdateDate(LocalDateTime.now());
				return entity;
			}).
			flatMap(entity -> recommendationRepository.save(entity).
				onErrorMap(DuplicateKeyException.class, 
						e -> new InvalidInputException(String.format("Duplicate key : check the recommendationID (%d).", 
								recommendationID))).
				log()).
			map(mapper::toModel);
	}

	/**
	 * {@inheritDoc}
	 */
	@ResponseStatus(value=HttpStatus.OK)
	@Override
	public Mono<Void> deleteRecommendation(Integer recommendationID) {
		
		if(recommendationID < 1) throw new InvalidInputException("RecommendationID should be greater than 0");
		
		return recommendationRepository.findByRecommendationID(recommendationID).
			switchIfEmpty(Mono.error(new NotFoundException(String.format("Recommendation with recommendationID=%d doesn't not exists.",
					recommendationID)))).
			log().
			flatMap(entity -> recommendationRepository.delete(entity));
	}
}
