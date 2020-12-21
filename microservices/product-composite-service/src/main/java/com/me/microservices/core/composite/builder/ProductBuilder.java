package com.me.microservices.core.composite.builder;

import java.time.LocalDateTime;

import com.me.microservices.core.product.api.model.Product;

public class ProductBuilder {

	private ProductBuilder() {}
	
	private Integer productID;
	private String name;
	private Integer weight;
	private LocalDateTime creationDate;
	private LocalDateTime updateDate;
	
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
	
	public ProductBuilder initCreationDate() {
		this.creationDate = LocalDateTime.now();
		return this;
	}
	
	public ProductBuilder initUpdateDate() {
		this.updateDate = LocalDateTime.now();
		return this;
	}
	
	public Product build() {
		
		Product p = new Product();
		p.setProductID(productID);
		p.setName(name);
		p.setWeight(weight);
		p.setCreationDate(creationDate);
		p.setUpdateDate(updateDate);
		return p;
	}
}
