package com.me.work.example.microservices.core.review.bo;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor @Data
@Entity
@Table(name="REVIEW", catalog="reviewdb")
public class ReviewEntity implements Serializable {

	@Id
	@Column(name="ID")
	@lombok.EqualsAndHashCode.Exclude
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	@NotNull
	@Column(name="REVIEW_ID", unique=true)
	private Integer reviewID;
	
	@NotNull
	@Column(name="PRODUCT_ID")
	private Integer productID;
	
	@NotEmpty
	@Column(name="AUTHOR")
	private String author;
	
	@NotEmpty
	@Column(name="SUBJECT")
	private String subject;
	
	@NotEmpty
	@Column(name="CONTENT")
	private String content;
	
	@Column(name="CREATION_DATE")
	private LocalDateTime creationDate;
	
	@Column(name="UPDATE_DATE")
	private LocalDateTime updateDate;
	
	private static final long serialVersionUID = 1L;
}
