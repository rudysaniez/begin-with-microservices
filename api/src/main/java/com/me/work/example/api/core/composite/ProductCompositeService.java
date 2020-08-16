package com.me.work.example.api.core.composite;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.me.work.example.api.Api;

public interface ProductCompositeService {

	/**
	 * @param id
	 * @return {@link ProductAggregate}
	 */
	@GetMapping(value=Api.PRODUCT_COMPOSITE_PATH + "/{productId}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ProductAggregate getProductAggregate(@PathVariable(name="productId", required=true) String productId);
}
