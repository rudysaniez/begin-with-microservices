package com.me.work.example.microservices.core.recommendation.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.me.work.example.api.core.common.PageMetadata;
import com.me.work.example.api.core.common.Paged;
import com.me.work.example.api.core.recommendation.Recommendation;
import com.me.work.example.api.core.recommendation.RecommendationService;
import com.me.work.example.handler.exception.InvalidInputException;
import com.me.work.example.handler.exception.NotFoundException;
import com.me.work.example.microservices.core.recommendation.Application.PaginationInformation;
import com.me.work.example.microservices.core.recommendation.bo.RecommendationEntity;
import com.me.work.example.microservices.core.recommendation.mapper.RecommendationMapper;
import com.me.work.example.microservices.core.recommendation.repository.RecommendationRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
	@Override
	public ResponseEntity<Recommendation> getRecommendation(Integer recommendationID) {
		
		if(recommendationID < 1) throw new InvalidInputException("RecommendationID should be greater than 0");
		
		RecommendationEntity recommendationEnity = recommendationRepository.findByRecommendationID(recommendationID).
				orElseThrow( () -> new NotFoundException(String.format("The recommendation with recommendationID=%d doesn't not exists.", 
						recommendationID)));
		
		log.debug("Recommendation with id={} has been found.", recommendationID);
			
		return ResponseEntity.ok(mapper.toModel(recommendationEnity));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<Paged<Recommendation>> getRecommendationByProductId(Integer productId, Integer pageNumber, Integer pageSize) {

		if(pageNumber == null) pageNumber = pagination.getPageNumber();
		if(pageSize == null) pageSize = pagination.getPageSize();
		
		if(productId < 1) throw new InvalidInputException("ProductID should be greater than 0");
		if(pageNumber < 0) throw new InvalidInputException("Page number should be greater or equal than 0");
		if(pageSize < 1) throw new InvalidInputException("Page size should be greater than 0");
		
		Page<RecommendationEntity> pageOfRecommendation = recommendationRepository.findByProductID(productId, 
				PageRequest.of(pageNumber, pageSize));
		
		log.debug("{} recommendations found by productID={}.", pageOfRecommendation.getTotalElements(), productId);
		
		return ResponseEntity.ok(toPaged(pageOfRecommendation));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<Recommendation> save(Recommendation recommendation) {
			
		try {
			
			RecommendationEntity recommendationEntity = mapper.toBusinessObject(recommendation);
			recommendationEntity.setCreationDate(LocalDateTime.now());
			
			recommendationEntity = recommendationRepository.save(recommendationEntity);
			
			log.debug("This recommendation has been saved : {}.", mapper.toModel(recommendationEntity));
			
			return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toModel(recommendationEntity));
		}
		catch(DuplicateKeyException e) {
			throw new InvalidInputException(String.format("Duplicate key : check the recommendationID (%d).", 
					recommendation.getRecommendationID()));
		}
		
	}

	/**
	 * {@link Recommendation}
	 */
	@Override
	public ResponseEntity<Recommendation> update(Recommendation recommendation, Integer recommendationID) {
		
		try {
			
			RecommendationEntity recommendationEntity = recommendationRepository.findByRecommendationID(recommendationID).
				orElseThrow(() -> new NotFoundException(String.format("The recommendation with recommendationID=%d doesn't not exists.", recommendationID)));
		
			recommendationEntity.setAuthor(recommendation.getAuthor());
			recommendationEntity.setContent(recommendation.getContent());
			recommendationEntity.setProductID(recommendation.getProductID());
			recommendationEntity.setRate(recommendation.getRate());
			recommendationEntity.setUpdateDate(LocalDateTime.now());
			
			recommendationEntity = recommendationRepository.save(recommendationEntity);
			
			return ResponseEntity.ok(mapper.toModel(recommendationEntity));
			
		}
		catch(DuplicateKeyException e) {
			throw new InvalidInputException(String.format("Duplicate key : check the recommendationID (%d).", recommendationID));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@ResponseStatus(value=HttpStatus.OK)
	@Override
	public void delete(Integer recommendationID) {
		
		Optional<RecommendationEntity> optOfRecommendation = recommendationRepository.findByRecommendationID(recommendationID);
		
		if(optOfRecommendation.isPresent())
			recommendationRepository.delete(optOfRecommendation.get());
		else
			throw new NotFoundException(String.format("The recommendation with ID=%d doesn't not exists.", recommendationID));
	}
	
	/**
	 * @param pageOfRecommendationEntity
	 * @return page of {@link Recommendation}
	 */
	private Paged<Recommendation> toPaged(Page<RecommendationEntity> pageOfRecommendationEntity) {
		
		Page<Recommendation> pageOfRecommendation = pageOfRecommendationEntity.map(r -> mapper.toModel(r));
		
		PageMetadata metadata = new PageMetadata(pageOfRecommendation.getSize(), pageOfRecommendation.getTotalElements(), 
				pageOfRecommendation.getTotalPages(), pageOfRecommendation.getNumber());
		
		return new Paged<>(pageOfRecommendation.getContent(), metadata);
	}
}
