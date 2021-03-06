package com.me.api.core.product;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;
import lombok.NoArgsConstructor;

@Deprecated
@NoArgsConstructor @Data
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
	
	public Product(Integer productID, String name, Integer weight) {
		
		this.productID = productID;
		this.name = name;
		this.weight = weight;
		this.creationDate = LocalDateTime.now();
		this.updateDate = null;
	}
}
