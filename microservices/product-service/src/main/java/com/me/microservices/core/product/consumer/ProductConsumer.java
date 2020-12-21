package com.me.microservices.core.product.consumer;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.me.api.event.Event;
import com.me.handler.exception.NotFoundException;
import com.me.microservices.core.product.api.ProductsApi;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProductConsumer {

	private final ProductsApi productService;
	
	@Autowired
	public ProductConsumer(ProductsApi productService) {
		this.productService = productService;
	}
	
	/**
	 * @param event
	 */
	@StreamListener(value = Sink.INPUT)
	public void consume(@Payload(required = false) Event<Integer> event) {

		if(event == null)
			return;
		
		log.info(" > Receive an event of type {}, created at {}", event.getType(), event.getCreationDate());
		
		try {
			switch(event.getType()) {
			
			case DELETE:
				productService.deleteProduct(event.getKey(), null).block();
				log.info(" > The product with id={} has been deleted at {}", event.getKey(), LocalDateTime.now());
				break;
			}
		}
		catch(NotFoundException nfe) {
			log.warn(String.format("The product with id=%d can't be deleted because it doesn't not exists.", event.getKey()), nfe);
		}
	}
}
