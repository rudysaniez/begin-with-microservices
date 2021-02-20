package com.me.microservices.core.review.bo;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
@CompoundIndex(name="review_comp_IDX", unique=true, def="{'reviewID':1, 'productID':1}")
@Document(collection = "reviews")
public class ReviewEntity implements Serializable {

	@Id
	private String id;
	
	@Indexed(name = "reviewId_IDX", unique = true, direction = IndexDirection.ASCENDING) @NotNull
	private Integer reviewID;
	
	@NotNull
	private Integer productID;
	
	@NotEmpty
	private String author;
	
	@NotEmpty
	private String subject;
	
	@NotEmpty
	private String content;
	
	@Version
	private Integer version;
	
	@CreatedDate
	private LocalDateTime creationDate;
	
	@LastModifiedDate
	private LocalDateTime updateDate;
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param id
	 * @param reviewID
	 * @param productID
	 * @param author
	 * @param subject
	 * @param content
	 */
	@PersistenceConstructor
	public ReviewEntity(String id, Integer reviewID, Integer productID, String author, String subject, String content) {
		
		this.id = id;
		this.reviewID = reviewID;
		this.productID = productID;
		this.author = author;
		this.subject = subject;
		this.content = content;
		this.creationDate = LocalDateTime.now();
	}
	
	/**
	 * @param id
	 * @return {@link ReviewEntity}
	 */
	public ReviewEntity withId(String id) {
		return new ReviewEntity(id, this.reviewID, this.productID, this.author, this.subject, this.content);
	}
}
