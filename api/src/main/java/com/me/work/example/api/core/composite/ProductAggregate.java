package com.me.work.example.api.core.composite;

import java.util.List;

import com.me.work.example.api.core.product.Product;
import com.me.work.example.api.core.recommendation.Recommendation;
import com.me.work.example.api.core.review.Review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;

@AllArgsConstructor
@Data
public class ProductAggregate {

	private final String productId;
	
	private final Product product;
	
	@Exclude
	private final List<Recommendation> recommendations;
	
	@Exclude
	private final List<Review> reviews;
}
