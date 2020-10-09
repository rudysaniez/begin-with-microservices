package com.me.work.example.api.core.recommendation;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor @Data
public class Recommendation {

	private Integer recommendationID;
	
	@Exclude
	private Integer productID;
	
	@Exclude
	private String author;
	
	@Exclude
	private Integer rate;
	
	@Exclude
	private String content;
	
	@Exclude
	private LocalDateTime creationDate;
	
	@Exclude
	private LocalDateTime updateDate;
}
