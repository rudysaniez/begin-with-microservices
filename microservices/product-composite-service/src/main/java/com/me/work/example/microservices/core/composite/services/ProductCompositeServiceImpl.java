package com.me.work.example.microservices.core.composite.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.me.work.example.api.core.composite.ProductAggregate;
import com.me.work.example.api.core.composite.ProductCompositeService;
import com.me.work.example.api.core.product.Product;
import com.me.work.example.api.core.recommendation.Recommendation;
import com.me.work.example.api.core.review.Review;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

	private ProductCompositeIntegration integration;
	
	@Autowired
	public ProductCompositeServiceImpl(ProductCompositeIntegration integration) {
		this.integration = integration;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ProductAggregate getProductAggregate(String productId) {
		
		Product product = this.integration.getProduct(productId);
		List<Recommendation> recommendations = this.integration.getRecommendationByProductId(productId);
		List<Review> reviews = this.integration.getReviewByProductId(productId);
		
		return new ProductAggregate(productId, product, recommendations, reviews);
	}
}
