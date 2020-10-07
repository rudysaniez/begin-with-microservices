package com.me.work.example.api.core.product;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.me.work.example.api.Api;
import com.me.work.example.api.core.common.Paged;

public interface ProductService {

	/**
	 * @param productID
	 * @return {@link Product}
	 */
	@GetMapping(value=Api.PRODUCT_PATH + "/{productID}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Product> getProduct(@PathVariable(name="productID", required=true) Integer productID);
	
	/**
	 * @param name
	 * @param pageNumber
	 * @param pageSize
	 * @return page of {@link Product}
	 */
	@GetMapping(value=Api.PRODUCT_PATH, produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Paged<Product>> findByName(@RequestParam(name="name", required=true) String name,
			@RequestParam(name="pageNumber", required=false) Integer pageNumber,
			@RequestParam(name="pageSize", required=false) Integer pageSize);
	
	/**
	 * @param product
	 * @return {@link Product}
	 */
	@PostMapping(value=Api.PRODUCT_PATH, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Product> save(@RequestBody Product product);
	
	/**
	 * @param product
	 * @param ProductID
	 * @return {@link Product}
	 */
	@PutMapping(value=Api.PRODUCT_PATH + "/{productID}", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Product> update(@RequestBody Product product, @PathVariable(name="productID", required=true) Integer productID);
	
	/**
	 * @param productID
	 */
	@DeleteMapping(value=Api.PRODUCT_PATH + "/{productID}")
	public void delete(@PathVariable(name="productID", required=true) Integer productID);
}
