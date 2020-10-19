package com.me.microservices.core.product.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;
import org.mapstruct.factory.Mappers;

import com.me.api.core.product.Product;
import com.me.microservices.core.product.bo.ProductEntity;
import com.me.microservices.core.product.mapper.ProductMapper;

public class MapperTest {

	private ProductMapper mapper = Mappers.getMapper(ProductMapper.class);
	
	@Test
	public void mapper() {
		
		ProductEntity productEntity = new ProductEntity(1, "Marteau vertueux", 2);
		
		Product product = mapper.toModel(productEntity);
		assertEquals(productEntity.getName(), product.getName());
		assertEquals(productEntity.getProductID(), product.getProductID());
		assertEquals(productEntity.getWeight(), product.getWeight());
		
		productEntity = mapper.toEntity(product);
		assertEquals(productEntity.getName(), product.getName());
		assertEquals(productEntity.getProductID(), product.getProductID());
		assertEquals(productEntity.getWeight(), product.getWeight());
		assertNotNull(productEntity.getCreationDate());
		assertNull(productEntity.getUpdateDate());
		assertNull(productEntity.getVersion());
	}
}
