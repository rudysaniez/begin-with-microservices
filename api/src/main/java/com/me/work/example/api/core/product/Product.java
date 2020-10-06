package com.me.work.example.api.core.product;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor @Data
public class Product {

	private Integer productID;
	
	@Exclude
	private String name;
	
	@Exclude
	private Integer weight;
	
	@Exclude
	private LocalDateTime creationDate;
	
	@Exclude
	private LocalDateTime updateDate;
}
