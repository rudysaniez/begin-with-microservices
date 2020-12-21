package com.me.microservices.core.composite.builder;

import java.time.LocalDateTime;

import com.me.microservices.core.recommendation.api.model.Recommendation;

public class RecommendationBuilder {

		private Integer recommendationID;
		private Integer productID;
		private String author;
		private Integer rate;
		private String content;
		private LocalDateTime creationDate;
		private LocalDateTime updateDate;
		
		private RecommendationBuilder() {}
		
		public static RecommendationBuilder create() {
			return new RecommendationBuilder();
		}
		
		public RecommendationBuilder withRecommendationID(Integer recommendationID) {
			this.recommendationID = recommendationID;
			return this;
		}
		
		public RecommendationBuilder withProductID(Integer productID) {
			this.productID = productID;
			return this;
		}
		
		public RecommendationBuilder withAuthor(String author) {
			this.author = author;
			return this;
		}
		
		public RecommendationBuilder withRate(Integer rate) {
			this.rate = rate;
			return this;
		}
		
		public RecommendationBuilder withContent(String content) {
			this.content = content;
			return this;
		}
		
		public RecommendationBuilder withCreationDate(LocalDateTime creationDate) {
			this.creationDate = creationDate;
			return this;
		}
		
		public Recommendation build() {
			
			Recommendation r = new Recommendation();
			r.setAuthor(author);
			r.setContent(content);
			r.setProductID(productID);
			r.setRate(rate);
			r.setRecommendationID(recommendationID);
			r.setCreationDate(creationDate);
			r.setUpdateDate(updateDate);
			return r;
		}
}
