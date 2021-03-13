package com.me.microservices.core.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import com.me.microservices.core.product.bo.ProductEntity;
import com.me.microservices.core.product.mapper.ProductMapper;
import com.me.microservices.core.product.mapper.ProductMapperImpl;

import lombok.Getter;
import lombok.Setter;

@EnableBinding(value = Sink.class)
@EnableReactiveMongoRepositories
@EnableConfigurationProperties(value={Application.PaginationInformation.class})
@ComponentScan(basePackages= {"com.me.microservices.core", "com.me.handler.http"})
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Bean
	public ProductMapper productMapper() {
		return new ProductMapperImpl();
	}
	
	@Getter @Setter
	@ConfigurationProperties(prefix="api.pagination")
	public static class PaginationInformation {
		
		private int pageNumber;
		private int pageSize;
	}
 	
	@Autowired
	private ReactiveMongoOperations mongoTemplate;
	
	@EventListener(ContextRefreshedEvent.class)
	public void initIndicesAfterStartup() {

		MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext = mongoTemplate.getConverter().getMappingContext();
		IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);

		ReactiveIndexOperations indexOps = mongoTemplate.indexOps(ProductEntity.class);
		resolver.resolveIndexFor(ProductEntity.class).forEach(e -> indexOps.ensureIndex(e).block());
	}
}
