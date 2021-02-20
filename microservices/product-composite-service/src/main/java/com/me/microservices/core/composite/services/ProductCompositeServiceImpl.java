package com.me.microservices.core.composite.services;

import java.util.Collections;
import java.util.List;

import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.me.handler.exception.DeletionException;
import com.me.microservices.core.composite.Application.PaginationInformation;
import com.me.microservices.core.composite.integration.ProductIntegration;
import com.me.microservices.core.composite.integration.RecommendationIntegration;
import com.me.microservices.core.composite.integration.ReviewIntegration;
import com.me.microservices.core.composite.mapper.PagedMapper;
import com.me.microservices.core.composite.mapper.RecommendationMapper;
import com.me.microservices.core.composite.mapper.ReviewMapper;
import com.me.microservices.core.product.api.model.Product;
import com.me.microservices.core.productcomposite.api.model.PagedRecommendationSummary;
import com.me.microservices.core.productcomposite.api.model.PagedReviewSummary;
import com.me.microservices.core.productcomposite.api.model.ProductAggregate;
import com.me.microservices.core.productcomposite.api.model.ProductComposite;
import com.me.microservices.core.productcomposite.api.model.RecommendationSummary;
import com.me.microservices.core.productcomposite.api.model.ReviewSummary;
import com.me.microservices.core.recommendation.api.model.PagedRecommendation;
import com.me.microservices.core.recommendation.api.model.Recommendation;
import com.me.microservices.core.review.api.model.PagedReview;
import com.me.microservices.core.review.api.model.Review;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author rudysaniez @since 0.0.1
 */
@RestController
public class ProductCompositeServiceImpl implements ProductsCompositeApi {

	@Autowired private ProductIntegration productIntegration;
	@Autowired private RecommendationIntegration recommendationIntegration;
	@Autowired private ReviewIntegration reviewIntegration;
	@Autowired private PaginationInformation pagination;
	
	private RecommendationMapper recommendationMapper = Mappers.getMapper(RecommendationMapper.class);
	private ReviewMapper reviewMapper = Mappers.getMapper(ReviewMapper.class);
	private PagedMapper pagedMapper = Mappers.getMapper(PagedMapper.class);
	
	private static final ServerWebExchange USELESS = null;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<ProductAggregate>> getCompositeProduct(Integer productId, Integer pageNumber, Integer pageSize, ServerWebExchange exchange) {
		
		if(pageNumber == null) pageNumber = pagination.getPageNumber();
		if(pageSize == null) pageSize = pagination.getPageSize();
		
		return Mono.zip(values -> createProductAggregate((Product)values[0], (PagedRecommendation)values[1], (PagedReview)values[2]), 
				productIntegration.getProduct(productId, USELESS).map(re -> re.getBody()),
				recommendationIntegration.getRecommendationByProductId(productId, pageNumber, pageSize, USELESS).map(re -> re.getBody()),
				reviewIntegration.getReviewByProductId(productId, pageNumber, pageSize, USELESS).map(re -> re.getBody())).
				map(pa -> ResponseEntity.ok(pa)).
				log();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Mono<ResponseEntity<ProductComposite>> createCompositeProduct(ProductComposite productComposite, ServerWebExchange exchange) {
		
		/**
		 * Préparation des flux.
		 * Un Mono pour le produit.
		 * Un Flux pour les recommendations.
		 * Un Flux pour les reviews.
		 */
		
		Mono<Product> monoOfProduct = Mono.just(productComposite).
				map(pc -> ProductBuilder.create().
										withProductID(pc.getProductID()).
										withName(pc.getName()).
										withWeight(pc.getWeight()).
										build()).
				log();
		
		Flux<Recommendation> fluxOfRecommendation = Flux.fromIterable(productComposite.getRecommendations() != null ? productComposite.getRecommendations() : Collections.<RecommendationSummary>emptyList()).
				map(recommendationMapper::toCoreModel).
				map(r -> {r.setProductID(productComposite.getProductID());return r;}).
				log();
		
		Flux<Review> fluxOfReview = Flux.fromIterable(productComposite.getReviews() != null ? productComposite.getReviews() : Collections.<ReviewSummary>emptyList()).
				map(reviewMapper::toCoreModel).
				map(r -> {r.setProductID(productComposite.getProductID());return r;}).
				log();
		
		return Mono.zip(values -> createProductComposite((Product)values[0], 
				(List<Recommendation>)values[1], (List<Review>)values[2]),
			
				productIntegration.save(monoOfProduct, USELESS).log().
					map(rs -> rs.getBody())
				,
				fluxOfRecommendation.flatMap(r -> recommendationIntegration.save(Mono.just(r), USELESS)).log().
				map(re -> re.getBody()).collectList()
				,
				fluxOfReview.flatMap(r -> reviewIntegration.save(Mono.just(r), USELESS)).log().
				map(re -> re.getBody()).collectList()
		).
		map(pc -> ResponseEntity.ok(pc)).
		log();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<Void>> deleteCompositeProduct(Integer productID, ServerWebExchange exchange) {
		
		try {
			
			productIntegration.deleteProductAsync(productID);
			recommendationIntegration.deleteRecommendationsAsync(productID);
			reviewIntegration.deleteReviewsAsync(productID);
		}
		catch(Exception e) {
			throw new DeletionException("Deletion has failed", e);
		}
		
		return Mono.<Void>empty().map(v -> ResponseEntity.ok(v));
		
	}

	/**
	 * @param product
	 * @param recommendations
	 * @param reviews
	 * @return {@link ProductAggregate}
	 */
	private ProductAggregate createProductAggregate(Product product, PagedRecommendation recommendations, 
			PagedReview reviews) {
		
		return AggregateBuilder.create().withProductID(product.getProductID()).
				withName(product.getName()).withWeight(product.getWeight()).
				withRecommendations(pagedMapper.toPagedRecommendationSummary(recommendations)).
				withReviews(pagedMapper.toPageReviewSummary(reviews)).
				build();
	}
	
	private ProductComposite createProductComposite(Product product, List<Recommendation> recommendations, List<Review> reviews) {
		
		return CompositeBuilder.create().withProductID(product.getProductID()).withName(product.getName()).
				withWeight(product.getWeight()).
				withRecommendations(recommendationMapper.toSummaries(recommendations)).
				withReviews(reviewMapper.toSummaries(reviews)).build();
	}
	
	protected static class ProductBuilder {
		
		private Integer productID;
		private String name;
		private Integer weight;
		
		private ProductBuilder() {}
		
		public static ProductBuilder create() {
			return new ProductBuilder();
		}
		
		public ProductBuilder withProductID(Integer productID) {
			this.productID = productID;
			return this;
		}
		
		public ProductBuilder withName(String name) {
			this.name = name;
			return this;
		}
		
		public ProductBuilder withWeight(Integer weight) {
			this.weight = weight;
			return this;
		}
		
		public Product build() {
			
			Product product = new Product();
			product.setProductID(productID);
			product.setName(name);
			product.setWeight(weight);
			return product;
		}
	}
	
	protected static class CompositeBuilder {
		
		private Integer productID;
		private String name;
		private Integer weight;
		private List<RecommendationSummary> recommendations;
		private List<ReviewSummary> reviews;
		
		private CompositeBuilder() {}
		
		public static CompositeBuilder create() {
			return new CompositeBuilder();
		}
		
		public CompositeBuilder withProductID(Integer productID) {
			this.productID = productID;
			return this;
		}
		
		public CompositeBuilder withName(String name) {
			this.name = name;
			return this;
		}
		
		public CompositeBuilder withWeight(Integer weight) {
			this.weight = weight;
			return this;
		}
		
		public CompositeBuilder withRecommendations(List<RecommendationSummary> recommendations) {
			this.recommendations = recommendations;
			return this;
		}
		
		public CompositeBuilder withReviews(List<ReviewSummary> reviews) {
			this.reviews = reviews;
			return this;
		}
		
		public ProductComposite build() {
			
			ProductComposite pc = new ProductComposite();
			pc.setProductID(productID);
			pc.setName(name);
			pc.setWeight(weight);
			pc.setRecommendations(recommendations);
			pc.setReviews(reviews);
			return pc;
		}
	}
	
	protected static class AggregateBuilder {
		
		private Integer productID;
		private String name;
		private Integer weight;
		private PagedRecommendationSummary recommendations;
		private PagedReviewSummary reviews;
		
		private AggregateBuilder() {}
		
		public static AggregateBuilder create() {
			return new AggregateBuilder();
		}
		
		public AggregateBuilder withProductID(Integer productID) {
			this.productID = productID;
			return this;
		}
		
		public AggregateBuilder withName(String name) {
			this.name = name;
			return this;
		}
		
		public AggregateBuilder withWeight(Integer weight) {
			this.weight = weight;
			return this;
		}
		
		public AggregateBuilder withRecommendations(PagedRecommendationSummary recommendations) {
			this.recommendations = recommendations;
			return this;
		}
		
		public AggregateBuilder withReviews(PagedReviewSummary reviews) {
			this.reviews = reviews;
			return this;
		}
		
		public ProductAggregate build() {
			
			ProductAggregate pa = new ProductAggregate();
			pa.setName(name);
			pa.setProductID(productID);
			pa.setWeight(weight);
			pa.setRecommendations(recommendations);
			pa.setReviews(reviews);
			return pa;
		}
	}
}
