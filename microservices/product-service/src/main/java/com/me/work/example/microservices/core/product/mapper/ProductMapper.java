package com.me.work.example.microservices.core.product.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.me.work.example.api.core.product.Product;
import com.me.work.example.microservices.core.product.bo.ProductEntity;

@Mapper
public interface ProductMapper {

	/**
	 * @param product
	 * @return {@link Product}
	 */
	
	public Product toModel(ProductEntity product);
	
	/**
	 * @param product
	 * @return {@link ProductEntity}
	 */
	@Mappings(value= {
			@Mapping(target="version", ignore=true), 
			@Mapping(target="id", ignore=true)
	})
	public ProductEntity toEntity(Product product);
}
