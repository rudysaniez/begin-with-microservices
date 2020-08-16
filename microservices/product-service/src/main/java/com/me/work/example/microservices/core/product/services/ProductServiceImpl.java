package com.me.work.example.microservices.core.product.services;

import org.springframework.web.bind.annotation.RestController;

import com.me.work.example.api.core.product.Product;
import com.me.work.example.api.core.product.ProductService;
import com.me.work.example.handler.exception.InvalidInputException;
import com.me.work.example.handler.exception.NotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ProductServiceImpl implements ProductService {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Product getProduct(String id) {
		
		if(id.equals("13")) throw new NotFoundException(String.format("The product %s doesn't not exist", id));
		if(id.equals("15")) throw new InvalidInputException(String.format("The product %s is an invalid input", id));
		
		if(log.isDebugEnabled())
			log.debug("The product {} found", id);
		
		return new Product(id, "name-" + id, 123);
	}
}
