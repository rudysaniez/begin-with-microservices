package com.me.microservices.core.product.services;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.me.api.core.common.PageMetadata;
import com.me.api.core.common.Paged;
import com.me.api.core.product.Product;
import com.me.api.core.product.ProductService;
import com.me.handler.exception.InvalidInputException;
import com.me.handler.exception.NotFoundException;
import com.me.microservices.core.product.Application.PaginationInformation;
import com.me.microservices.core.product.bo.ProductEntity;
import com.me.microservices.core.product.mapper.ProductMapper;
import com.me.microservices.core.product.repository.ProductRepository;

import reactor.core.publisher.Mono;

@RestController
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final ProductMapper mapper;
	private final PaginationInformation pagination;
	
	/**
	 * @param productRepository
	 * @param productMapper
	 * @param pagination
	 */
	@Autowired
	public ProductServiceImpl(ProductRepository productRepository, ProductMapper mapper,
			PaginationInformation pagination) {
		
		this.productRepository = productRepository;
		this.mapper = mapper;
		this.pagination = pagination;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@ResponseStatus(value=HttpStatus.OK)
	@Override
	public Mono<Product> getProduct(Integer productID) {
		
		if(productID < 1) throw new InvalidInputException("ProductID should be greater than 0.");
		
		return productRepository.findByProductID(productID).
			switchIfEmpty(Mono.error(new NotFoundException(String.format("Product with productID=%d doesn't not exists.", productID)))).
			log().
			map(mapper::toModel);
	}

	/**
	 * {@inheritDoc}
	 */
	@ResponseStatus(value=HttpStatus.OK)
	@Override
	public Mono<Paged<Product>> findByName(String name, Integer pageNumber, Integer pageSize) {

		if(StringUtils.isEmpty(name)) throw new InvalidInputException("Name should not be empty.");
		
		if(pageNumber == null || pageNumber < 0) pageNumber = pagination.getPageNumber();
		if(pageSize == null || pageSize < 1) pageSize = pagination.getPageSize();
		
		final Integer pSize = pageSize;
		
		final Pageable page = PageRequest.of(pageNumber, pageSize, Sort.by(Direction.ASC, "productID"));
		
		return productRepository.countByNameStartingWith(name.toUpperCase()).
				flatMap(count -> productRepository.findByNameStartingWith(name.toUpperCase(), page).
				map(mapper::toModel).
				collectList().map(list -> new Paged<Product>(list, 
						new PageMetadata(page.getPageSize(), count, count < pSize ? 1 : count % pSize == 0 ? count/pSize : ((count/pSize) + 1), page.getPageNumber()))));
	}

	/**
	 * {@inheritDoc}
	 */
	@ResponseStatus(value=HttpStatus.CREATED)
	@Override
	public Mono<Product> save(Product product) {
			
		if(product.getProductID() < 1) throw new InvalidInputException("ProductID should be greater than 0.");
		if(product.getName().isEmpty()) throw new InvalidInputException("Product name should be not empty.");
		
		ProductEntity productEntity = mapper.toEntity(product);
		productEntity.setName(product.getName().toUpperCase());
		productEntity.setCreationDate(LocalDateTime.now());
		
		return productRepository.save(productEntity).log().
				onErrorMap(DuplicateKeyException.class, e -> new InvalidInputException(String.format("Duplicate key : check the productID (%d) or the name (%s) of product.", 
						product.getProductID(), product.getName()))).
				log().
				map(mapper::toModel);
	}

	/**
	 * {@inheritDoc}
	 */
	@ResponseStatus(value=HttpStatus.OK)
	@Override
	public Mono<Product> update(Product product, Integer productID) {
		
		if(product.getProductID() < 1) throw new InvalidInputException("ProductID should be greater than 0.");
		if(product.getName().isEmpty()) throw new InvalidInputException("Product name should be not empty.");
		
		return productRepository.findByProductID(productID).
				switchIfEmpty(Mono.error(new NotFoundException(String.format("Product with productID=%d doesn't not exists.", productID)))).
				log().
				map(entity -> {
					
					entity.setName(product.getName().toUpperCase());
					entity.setWeight(product.getWeight());
					entity.setUpdateDate(LocalDateTime.now());
					return entity;
				}).
				flatMap(entity -> productRepository.save(entity).
						onErrorMap(DuplicateKeyException.class, e -> new InvalidInputException(String.
								format("Duplicate key : check the productID (%d) or the name (%s) of product.", 
										product.getProductID(), product.getName()))).
						log().
						map(mapper::toModel));
	}

	/**
	 * {@inheritDoc}
	 */
	@ResponseStatus(value=HttpStatus.OK)
	@Override
	public Mono<Void> deleteProduct(Integer productID) {

		if(productID < 1) throw new InvalidInputException("ProductID should be greater than 0.");
		
		return productRepository.findByProductID(productID).
			switchIfEmpty(Mono.error(new NotFoundException(String.format("Product with productID=%d doesn't not exists.", productID)))).
			log().
			flatMap(entity -> productRepository.delete(entity));
	}
}
