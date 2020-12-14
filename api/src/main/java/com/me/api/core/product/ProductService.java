package com.me.api.core.product;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.me.api.Api;
import com.me.api.core.common.Paged;

import reactor.core.publisher.Mono;

@Deprecated
public interface ProductService {

	/**
	 * @param productID
	 * @return mono of {@link Product}
	 */
	@GetMapping(value=Api.PRODUCT_PATH + "/{productID}", produces=MediaType.APPLICATION_JSON_VALUE)
	public Mono<Product> getProduct(@PathVariable(name="productID", required=true) Integer productID);
	
	/**
	 * @param name
	 * @param pageNumber
	 * @param pageSize
	 * @return mono of {@link Paged}
	 */
	@GetMapping(value=Api.PRODUCT_PATH, produces=MediaType.APPLICATION_JSON_VALUE)
	public Mono<Paged<Product>> findByName(@RequestParam(name="name", required=true) String name,
			@RequestParam(name="pageNumber", required=false) Integer pageNumber,
			@RequestParam(name="pageSize", required=false) Integer pageSize);
	
	/**
	 * @param product
	 * @return mono of {@link Product}
	 */
	@PostMapping(value=Api.PRODUCT_PATH, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public Mono<Product> save(@RequestBody Product product);
	
	/**
	 * @param product
	 * @param ProductID
	 * @return mono of {@link Product}
	 */
	@PutMapping(value=Api.PRODUCT_PATH + "/{productID}", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public Mono<Product> update(@RequestBody Product product, @PathVariable(name="productID", required=true) Integer productID);
	
	/**
	 * @param productID
	 * @return mono void
	 */
	@DeleteMapping(value=Api.PRODUCT_PATH + "/{productID}")
	public Mono<Void> deleteProduct(@PathVariable(name="productID", required=true) Integer productID);
}
