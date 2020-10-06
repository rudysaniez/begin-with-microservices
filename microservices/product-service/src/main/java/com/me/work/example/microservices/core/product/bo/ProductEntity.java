package com.me.work.example.microservices.core.product.bo;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
@Document(collection="products")
public class ProductEntity {

	@Id
	private String id;

	@Indexed(unique=true, name="productId_IDX", direction=IndexDirection.ASCENDING)
	private int productID;
	
	@Indexed(unique=true, name="productName_IDX", direction=IndexDirection.ASCENDING)
	private String name;
	
	private Integer weight;
	
	@Version
	private Integer version;
	
	@CreatedDate
	private LocalDateTime creationDate;
	
	@LastModifiedDate
	private LocalDateTime updateDate;
	
	/**
	 * @param productID
	 * @param name
	 * @param weight
	 */
	public ProductEntity(int productID, String name, Integer weight) {
		
		this.productID = productID;
		this.name = name;
		this.weight = weight;
		this.creationDate = LocalDateTime.now();
		this.updateDate = null;
		this.version = null;
	}
}
