package com.me.microservices.core.product.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.me.handler.exception.InvalidInputException;
import com.me.handler.exception.NotFoundException;
import com.me.microservices.core.product.Application.PaginationInformation;
import com.me.microservices.core.product.api.ProductsApi;
import com.me.microservices.core.product.api.model.PageMetadata;
import com.me.microservices.core.product.api.model.PagedProduct;
import com.me.microservices.core.product.api.model.Product;
import com.me.microservices.core.product.bo.ProductEntity;
import com.me.microservices.core.product.mapper.ProductMapper;
import com.me.microservices.core.product.repository.ProductRepository;

import reactor.core.publisher.Mono;

@RestController
public class ProductServiceImpl implements ProductsApi {

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
	@Override
	public Mono<ResponseEntity<Product>> getProduct(Integer productID, ServerWebExchange exchange) {
		
		if(productID < 1) throw new InvalidInputException("ProductID should be greater than 0.");
		
		 return productRepository.findByProductID(productID).
				 switchIfEmpty(Mono.error(new NotFoundException(String.format("Product with productID=%d doesn't not exists.", productID)))).
				 log().
				 map(mapper::toModel).
				 map(p -> ResponseEntity.ok(p)).
				 log();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<PagedProduct>> findByName(String name, Integer pageNumber, Integer pageSize, ServerWebExchange exchange) {

		if(StringUtils.isEmpty(name)) throw new InvalidInputException("Name should not be empty.");
		
		if(pageNumber == null || pageNumber < 0) pageNumber = pagination.getPageNumber();
		if(pageSize == null || pageSize < 1) pageSize = pagination.getPageSize();
		
		final Pageable page = PageRequest.of(pageNumber, pageSize, Sort.by(Direction.ASC, "name", "productID"));
		final Integer pSize = pageSize;
		
		return productRepository.countByNameStartingWith(name.toUpperCase()).
				flatMap(count -> productRepository.findByNameStartingWith(name.toUpperCase(), page).
						log().
						map(mapper::toModel).
						collectList().
						map(list -> {
					
							PagedProduct pageProduct = new PagedProduct();
							pageProduct.setContent(list);
							
							PageMetadata pageMetadata = new PageMetadata();
							pageMetadata.setNumber(Integer.toUnsignedLong(page.getPageNumber()));
							pageMetadata.setSize(Integer.toUnsignedLong(page.getPageSize()));
							pageMetadata.setTotalElements(count);
							pageMetadata.setTotalPages(count < pSize ? 1 : count % pSize == 0 ? count/pSize : ((count/pSize) + 1));
							pageProduct.setPage(pageMetadata);
							
							return ResponseEntity.ok(pageProduct);
						})
				).log();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<Product>> save(Mono<Product> product, ServerWebExchange exchange) {
			
		return product.map(p -> {
				if(p.getProductID() < 1) throw new InvalidInputException("ProductID should be greater than 0.");
				if(p.getName().isEmpty()) throw new InvalidInputException("Product name should be not empty.");
				return p;
			}).
			map(mapper::toEntity).
			map(pe -> {
				pe.setName(pe.getName().toUpperCase());
				pe.setCreationDate(LocalDateTime.now());
				return pe;
			}).
			flatMap(pe -> productRepository.save(pe).
					onErrorMap(DuplicateKeyException.class, e -> new InvalidInputException(String.format("Duplicate key : check the productID (%d) or the name (%s) of product.", pe.getProductID(), pe.getName()))).log()).
			log().
			map(mapper::toModel).
			map(p -> ResponseEntity.status(HttpStatus.CREATED).body(p)).
			log();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<Product>> update(Integer productID, Mono<Product> product, ServerWebExchange exchange) {
		
		Mono<ProductEntity> modelToEntity = product.map(p -> {
			
				if(p.getProductID() < 1) throw new InvalidInputException("ProductID should be greater than 0.");
				if(p.getName().isEmpty()) throw new InvalidInputException("Product name should be not empty.");
				return p;
			}).map(mapper::toEntity);
		
		return productRepository.findByProductID(productID).
			switchIfEmpty(Mono.error(new NotFoundException(String.format("Product with productID=%d doesn't not exists.", productID)))).
			log().
			transform(m -> m.concatWith(modelToEntity).buffer().single().
					map(list -> {
						
						Optional<ProductEntity> productInDatabase = list.stream().filter(pe -> StringUtils.isNotBlank(pe.getId())).findFirst();
						Optional<ProductEntity> productModel = list.stream().filter(pe -> StringUtils.isBlank(pe.getId())).findFirst();
						
						if(productInDatabase.isPresent() && productModel.isPresent()) {
							
							productInDatabase.get().setName(productModel.get().getName().toUpperCase());
							productInDatabase.get().setWeight(productModel.get().getWeight());
							productInDatabase.get().setUpdateDate(LocalDateTime.now());
						}
						
						return productInDatabase.get();
					})).
			log().
			flatMap(pe -> productRepository.save(pe).
				onErrorMap(DuplicateKeyException.class, e -> new InvalidInputException(String.format("Duplicate key : check the productID (%d) or the name (%s) of product.", productID, pe.getName())))
			).
			log().
			map(mapper::toModel).
			map(pe -> ResponseEntity.ok(pe)).
			log();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<ResponseEntity<Void>> deleteProduct(Integer productID, ServerWebExchange exchange) {

		if(productID < 1) throw new InvalidInputException("ProductID should be greater than 0.");
		
		return productRepository.findByProductID(productID).
				switchIfEmpty(Mono.error(new NotFoundException(String.format("Product with productID=%d doesn't not exists.", productID)))).
				log().
				flatMap(entity -> productRepository.delete(entity)).
				map(v -> ResponseEntity.ok(v));
	}
}
