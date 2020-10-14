package com.me.work.example.microservices.core.product.services;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.me.work.example.api.core.common.PageMetadata;
import com.me.work.example.api.core.common.Paged;
import com.me.work.example.api.core.product.Product;
import com.me.work.example.api.core.product.ProductService;
import com.me.work.example.handler.exception.InvalidInputException;
import com.me.work.example.handler.exception.NotFoundException;
import com.me.work.example.microservices.core.product.Application.PaginationInformation;
import com.me.work.example.microservices.core.product.bo.ProductEntity;
import com.me.work.example.microservices.core.product.mapper.ProductMapper;
import com.me.work.example.microservices.core.product.repository.ProductRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
	@Override
	public ResponseEntity<Product> getProduct(Integer productID) {
		
		if(productID < 1) throw new InvalidInputException("ProductID should be greater than 0");
		
		ProductEntity productEntity = productRepository.findByProductID(productID).
				orElseThrow(() -> new NotFoundException(String.format("The product with productID=%d doesn't not exists.", productID)));
		
		log.debug("The product with productID={} found.", productID);
			
		return ResponseEntity.ok(mapper.toModel(productEntity));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<Paged<Product>> findByName(String name, Integer pageNumber, Integer pageSize) {

		if(StringUtils.isEmpty(name)) throw new InvalidInputException("Name should not be empty.");
		if(pageNumber == null) pageNumber = pagination.getPageNumber();
		if(pageSize == null) pageSize = pagination.getPageSize();
		
		Page<ProductEntity> pageOfProducts = productRepository.findByNameStartingWith(name.toUpperCase(), 
				PageRequest.of(pageNumber, pageSize));
		
		log.debug("{} products has been found by name starting with %s.", pageOfProducts.getTotalElements(), name);
		
		return ResponseEntity.ok(toPaged(pageOfProducts));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<Product> save(Product product) {
		
		try {
			
			ProductEntity productEntity = mapper.toEntity(product);
			productEntity.setName(product.getName().toUpperCase());
			productEntity.setCreationDate(LocalDateTime.now());
			
			productEntity = productRepository.save(productEntity);
			
			log.debug("This product has been saved : {}.", mapper.toModel(productEntity));
			
			return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toModel(productEntity));
		}
		catch(DuplicateKeyException e) {
			throw new InvalidInputException(String.format("Duplicate key : check the productID (%d) or the name (%s) of product.", 
					product.getProductID(), product.getName()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<Product> update(Product product, Integer productID) {
		
		try {
			
			ProductEntity productEntity = productRepository.findByProductID(productID).
					orElseThrow(() -> new NotFoundException(String.format("Product with productID=%d doesn't not exists.", productID)));
			
			productEntity.setName(product.getName().toUpperCase());
			productEntity.setWeight(product.getWeight());
			productEntity.setUpdateDate(LocalDateTime.now());
			
			productEntity = productRepository.save(productEntity);
			
			log.debug("This product has been updated : {}.", mapper.toModel(productEntity).toString());
			
			return ResponseEntity.ok(mapper.toModel(productEntity));
		}
		catch(DuplicateKeyException e) {
			throw new InvalidInputException(String.format("Duplicate key : check the name (%s) of product.", product.getName()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@ResponseStatus(value=HttpStatus.OK)
	@Override
	public void deleteProduct(Integer productID) {

		if(productID < 1) throw new InvalidInputException("ProductID should be greater than 0");
		
		ProductEntity productEntity = productRepository.findByProductID(productID).
				orElseThrow(() -> new NotFoundException(String.format("Product with productID=%d doesn't not exists", productID)));
			
		productRepository.delete(productEntity);
		
		log.info("This product has been deleted : {}.", mapper.toModel(productEntity).toString());
	}
	
	/**
	 * @param pageOfProductEntity
	 * @return page of {@link ProductEntity}
	 */
	private Paged<Product> toPaged(Page<ProductEntity> pageOfProductEntity) {
		
		Page<Product> pageOfProductModel = pageOfProductEntity.map(r -> mapper.toModel(r));
		
		PageMetadata metadata = new PageMetadata(pageOfProductModel.getSize(), pageOfProductModel.getTotalElements(), 
				pageOfProductModel.getTotalPages(), pageOfProductModel.getNumber());
		
		return new Paged<>(pageOfProductModel.getContent(), metadata);
	}
}
