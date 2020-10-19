package com.me.microservices.core.composite.services;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.me.api.core.common.Paged;
import com.me.api.core.composite.ProductAggregate;
import com.me.api.core.composite.ProductComposite;
import com.me.api.core.composite.ProductCompositeService;
import com.me.api.core.composite.RecommendationSummary;
import com.me.api.core.composite.ReviewSummary;
import com.me.api.core.product.Product;
import com.me.api.core.recommendation.Recommendation;
import com.me.api.core.review.Review;
import com.me.microservices.core.composite.Application.PaginationInformation;
import com.me.microservices.core.composite.mapper.RecommendationMapper;
import com.me.microservices.core.composite.mapper.ReviewMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * @author rudysaniez @since 0.0.1
 */
@Slf4j
@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

	private ProductCompositeIntegration integration;
	private RecommendationMapper recommendationMapper;
	private ReviewMapper reviewMapper;
	private PaginationInformation pagination;
	
	@Autowired
	public ProductCompositeServiceImpl(ProductCompositeIntegration integration, RecommendationMapper recommendationMapper,
			ReviewMapper reviewMapper, PaginationInformation pagination) {
		
		this.integration = integration;
		this.recommendationMapper = recommendationMapper;
		this.reviewMapper = reviewMapper;
		this.pagination = pagination;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<ProductAggregate> getCompositeProduct(Integer productID, Integer pageNumber, Integer pageSize) {
		
		if(pageNumber == null) pageNumber = pagination.getPageNumber();
		if(pageSize == null) pageSize = pagination.getPageSize();
		
		ResponseEntity<Product> product = integration.getProduct(productID);
		ResponseEntity<Paged<Recommendation>> pageOfRecommendations = integration.getRecommendationByProductId(productID, pageNumber, pageSize);
		ResponseEntity<Paged<Review>> pageOfReviews = integration.getReviewByProductId(productID, pageNumber, pageSize);
		
		return ResponseEntity.ok(buildProductAggregate(product.getBody(), pageOfRecommendations.getBody(), 
				pageOfReviews.getBody()));
	}

	/**
	 * {@inheritDoc}
	 */
	@ResponseStatus(value=HttpStatus.CREATED)
	@Override
	public void createCompositeProduct(ProductComposite body) {

		try {
			
			integration.save(new Product(body.getProductID(), body.getName(), body.getWeight()));
			
			if(body.getRecommendations() != null)
				body.getRecommendations().forEach(rs -> {
					Recommendation model = recommendationMapper.toModel(rs);
					model.setProductID(body.getProductID());
					integration.save(model);
				});
			
			if(body.getReviews() != null)
				body.getReviews().forEach(rs -> {
					Review model = reviewMapper.toModel(rs);
					model.setProductID(body.getProductID());
					integration.save(model);
				});
		}
		catch(RuntimeException e) {
			
			log.error(e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@ResponseStatus(value=HttpStatus.OK)
	@Override
	public void deleteCompositeProduct(Integer productID) {
		
		int currentPage = pagination.getPageNumber();
		int pageSize = pagination.getPageSize();
		
		/**
		 * Recommendations.
		 */
		ResponseEntity<Paged<Recommendation>> recommendations = integration.getRecommendationByProductId(productID, currentPage, pageSize);
		long totalPages = recommendations.getBody().getPage().getTotalPages();
		
		do {
			
			recommendations.getBody().getContent().forEach(r -> integration.deleteRecommendation(r.getRecommendationID()));
			currentPage++;
			
			if(currentPage < totalPages)
				recommendations = integration.getRecommendationByProductId(productID, currentPage, pageSize);
			
		}while(currentPage < totalPages);
		
		currentPage = 0;
		
		/**
		 * Reviews.
		 */
		ResponseEntity<Paged<Review>> reviews = integration.getReviewByProductId(productID, currentPage, pageSize);
		totalPages = reviews.getBody().getPage().getTotalPages();
		
		do {
			
			reviews.getBody().getContent().forEach(r -> integration.deleteReview(r.getReviewID()));
			currentPage++;
			
			if(currentPage < totalPages)
				reviews = integration.getReviewByProductId(productID, currentPage, pageSize);
			
		}while(currentPage < totalPages);
		
		/**
		 * Product.
		 */
		integration.deleteProduct(productID);
	}
	
	/**
	 * @return {@link ProductAggregate}
	 */
	private ProductAggregate buildProductAggregate(Product product, Paged<Recommendation> recommendations, Paged<Review> reviews) {
		
		Paged<RecommendationSummary> pageOfRecommendationSummaries = new Paged<RecommendationSummary>(
				recommendations.getContent().stream().
					map(r -> new RecommendationSummary(r.getRecommendationID(), r.getAuthor(), 
							r.getRate(), r.getContent())).collect(Collectors.toList()), recommendations.getPage());
		
		
		Paged<ReviewSummary> pageOfReviewSummaries = new Paged<ReviewSummary>(
				reviews.getContent().stream().
					map(r -> new ReviewSummary(r.getReviewID(), r.getAuthor(), r.getSubject(), 
							r.getContent())).collect(Collectors.toList()), reviews.getPage());
		
		return new ProductAggregate(product.getProductID(), product.getName(), product.getWeight(), 
				pageOfRecommendationSummaries, pageOfReviewSummaries);
	}
}
