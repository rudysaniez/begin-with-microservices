package com.me.work.example.microservices.core.recommendation.bo;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
@CompoundIndex(name="recom_comp_IDX", unique=true, def="{'recommendationID':1, 'productID':1}")
@Document(collection="recommendations")
public class RecommendationEntity {

	@Id
	private String id;
	
	@Indexed(unique=true, name="recommendationID_IDX", direction=IndexDirection.ASCENDING)
	private Integer recommendationID;
	
	private Integer productID;
	
	@Exclude
	private String author;
	
	@Exclude
	private Integer rate;
	
	@Exclude
	private String content;
	
	@Version
	private Integer version;
	
	@CreatedDate
	private LocalDateTime creationDate;
	
	@LastModifiedDate
	private LocalDateTime updateDate;
	
	/**
	 * @param recommendationID
	 * @param productID
	 * @param author
	 * @param rate
	 * @param content
	 */
	public RecommendationEntity(Integer recommendationID, Integer productID, String author, Integer rate, String content) {
		
		this.recommendationID = recommendationID;
		this.productID = productID;
		this.author = author;
		this.rate = rate;
		this.content =content;
		this.version = null;
		this.creationDate = LocalDateTime.now();
		this.updateDate = null;
	}
}
