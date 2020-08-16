package com.me.work.example.api.core.product;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.me.work.example.api.Api;

public interface ProductService {

	/**
	 * @param id
	 * @return {@link Product}
	 */
	@GetMapping(value=Api.PRODUCT_PATH + "/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
	public Product getProduct(@PathVariable(name="id", required=true) String id);
}
