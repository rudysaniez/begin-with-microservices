package com.me.microservices.core.recommendation.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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
import com.me.microservices.core.recommendation.Application.PaginationInformation;
import com.me.microservices.core.recommendation.api.RecommendationsApi;
import com.me.microservices.core.recommendation.api.model.PageMetadata;
import com.me.microservices.core.recommendation.api.model.PagedRecommendation;
import com.me.microservices.core.recommendation.api.model.Recommendation;
import com.me.microservices.core.recommendation.bo.RecommendationEntity;
import com.me.microservices.core.recommendation.mapper.RecommendationMapper;
import com.me.microservices.core.recommendation.repository.RecommendationRepository;

import reactor.core.publisher.Mono;

@RestController
public class RecommendationServiceImpl implements RecommendationsApi {

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
	@Override
	public Mono<ResponseEntity<Recommendation>> getRecommendation(Integer recommendationID, ServerWebExchange exchange) {
		
		if(recommendationID < 1) throw new InvalidInputException("RecommendationID should be greater than 0");
		
		return recommendationRepository.findByRecommendationID(recommendationID).
				switchIfEmpty(Mono.error(new NotFoundException(String.format("Recommendation with recommendationID=%d doesn't not exists.", 
					recommendationID)))).
				log().
				map(mapper::toModel).
				map(r -> ResponseEntity.ok(r)).
				log();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<PagedRecommendation>> getRecommendationByProductId(Integer productID, Integer pageNumber, Integer pageSize, ServerWebExchange exchange) {

		if(productID < 1) throw new InvalidInputException("ProductId should be greater than 0");
		if(pageNumber == null || pageNumber < 0) pageNumber = pagination.getPageNumber();
		if(pageSize == null || pageSize < 1) pageSize = pagination.getPageSize();
		
		Pageable page = PageRequest.of(pageNumber, pageSize, Sort.by(Direction.ASC, "recommendationID"));
		Integer pSize = pageSize;
		
		return recommendationRepository.countByProductID(productID).
				flatMap(count -> recommendationRepository.findByProductID(productID, page).
						log().
						map(mapper::toModel).
						collectList().
						map(list -> {
							
							PagedRecommendation pageRecommendation = new PagedRecommendation();
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
	public Mono<ResponseEntity<Recommendation>> save(Mono<Recommendation> recommendation, ServerWebExchange exchange) {
		
		return recommendation.map(r -> {
				if(r.getRecommendationID() < 1) throw new InvalidInputException("RecommendationID should be greater than 0.");
				return r;
			}).
			map(mapper::toEntity).
			map(re -> {
				re.setCreationDate(LocalDateTime.now());
				return re;
			}).
			flatMap(re -> recommendationRepository.save(re).
					onErrorMap(DuplicateKeyException.class, e -> new InvalidInputException(String.format("Duplicate key : check the recommendationID (%d).", re.getRecommendationID())))).
			log().
			map(mapper::toModel).
			map(r -> ResponseEntity.status(HttpStatus.CREATED).body(r)).
			log();
	}

	/**
	 * {@link Recommendation}
	 */
	@Override
	public Mono<ResponseEntity<Recommendation>> update(Integer recommendationID, Mono<Recommendation> recommendation, ServerWebExchange exchange) {
		
		if(recommendationID < 1) throw new InvalidInputException("RecommendationID should be greater than 0.");
		
		Mono<RecommendationEntity> modelToEntity = recommendation.map(r -> {
			
			if(r.getRecommendationID() < 1) throw new InvalidInputException("RecommendationID should be greater than 0.");
			if(r.getProductID() < 1) throw new InvalidInputException("ProductID in Recommendation should be greater than 0.");
			return r;
		}).map(mapper::toEntity);
		
		return recommendationRepository.findByRecommendationID(recommendationID).
				switchIfEmpty(Mono.error(new NotFoundException(String.format("Recommendation with recommendationID=%d doesn't not exists.", recommendationID)))).
				log().
				transform(m -> m.concatWith(modelToEntity).buffer().single().
						map(list -> {
							
							Optional<RecommendationEntity> recommendationInDatabase = list.stream().filter(re -> StringUtils.isNotBlank(re.getId())).findFirst();
							Optional<RecommendationEntity> recommendationModel = list.stream().filter(re ->StringUtils.isBlank(re.getId())).findFirst();
							
							if(recommendationInDatabase.isPresent() && recommendationModel.isPresent()) {
								
								recommendationInDatabase.get().setAuthor(recommendationModel.get().getAuthor());
								recommendationInDatabase.get().setContent(recommendationModel.get().getContent());
								recommendationInDatabase.get().setProductID(recommendationModel.get().getProductID());
								recommendationInDatabase.get().setRate(recommendationModel.get().getRate());
								recommendationInDatabase.get().setUpdateDate(LocalDateTime.now());
							}
							
							return recommendationInDatabase.get();
						})
				).
				log().
				flatMap(re -> recommendationRepository.save(re).
					onErrorMap(DuplicateKeyException.class, e -> new InvalidInputException(String.format("Duplicate key : check the recommendationID (%d).", re.getRecommendationID())))
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
	public Mono<ResponseEntity<Void>> deleteRecommendations(Integer productID, ServerWebExchange exchange) {
		
		if(productID < 1) throw new InvalidInputException("ProductID should be greater than 0");
		
		 return recommendationRepository.deleteByProductID(productID).
				 map(v -> ResponseEntity.ok(v));
	}
}
