package com.me.work.example.api.core.review;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor @Data
public class Review  {

	private Integer reviewID;
	
	@Exclude
	private Integer productID;
	
	@Exclude
	private String author;
	
	@Exclude
	private String subject;
	
	@Exclude
	private String content;
	
	@Exclude
	private LocalDateTime creationDate;
	
	@Exclude
	private LocalDateTime updateDate;
}
