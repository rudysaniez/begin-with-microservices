package com.me.work.example.api.core.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;

@AllArgsConstructor
@Data
public class Product {

	private final String productID;
	
	@Exclude
	private final String name;
	
	@Exclude
	private final Integer weight;
	
	public Product() {
		
		this.productID = null;
		this.name = null;
		this.weight = null;
	}
}
