package com.me.microservices.core.recommendation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.me.microservices.core.recommendation.bo.RecommendationEntity;
import com.me.microservices.core.recommendation.mapper.RecommendationMapper;
import com.me.microservices.core.recommendation.mapper.RecommendationMapperImpl;

import lombok.Getter;
import lombok.Setter;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableConfigurationProperties(value= {Application.PaginationInformation.class, Application.PathInformation.class})
@EnableMongoRepositories
@ComponentScan(basePackages= {"com.me.microservices.core", "com.me.handler.http"})
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Bean
	public RecommendationMapper recommendationMapper() {
		return new RecommendationMapperImpl();
	}
	
	@Getter @Setter
	@ConfigurationProperties(prefix="api.pagination")
	public static class PaginationInformation {
		
		private int pageNumber;
		private int pageSize;
	}
	
	@Getter @Setter
	@ConfigurationProperties(prefix="spring.webflux")
	public static class PathInformation {
		
		private String basePath;
	}
	
	/**
	 * <pre>
	 * http://$HOST:$PORT/api/v1/swagger-ui.html
	 * </pre>
	 * @author rudysaniez
	 */
	@Profile("recommendation-swagger")
	@Configuration
	@EnableSwagger2
	public class SpringFoxSwagger {
		
		@Value("${api.common.version}") String version;
		@Value("${api.common.title}") String title;
		@Value("${api.common.description}") String description;
		@Value("${api.common.termsOfServiceUrl}") String termsOfServiceUrl;
		@Value("${api.common.license}") String license;
		@Value("${api.common.licenseUrl}") String licenseUrl;
		@Value("${api.common.contact.name}") String contactName;
		@Value("${api.common.contact.url}") String contactUrl;
		@Value("${api.common.contact.email}") String contactEmail;
		
		@Bean
	    public Docket api() { 
			
	        return new Docket(DocumentationType.SWAGGER_2)  
	          .select()                                  
	          .apis(RequestHandlerSelectors.basePackage("com.me.work.example.microservices.core.recommendation"))              
	          .paths(PathSelectors.any())                          
	          .build()
	          .apiInfo(new ApiInfo(title, description, version, termsOfServiceUrl, 
	        		  new Contact(contactName, contactUrl, contactEmail), 
	        		  	license, licenseUrl, java.util.Collections.emptyList()));
	    }
	}
	
	@Autowired
	private MongoOperations mongoTemplate;
	
	@EventListener(ContextRefreshedEvent.class)
	public void initIndicesAfterStartup() {

		MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext = mongoTemplate.getConverter().getMappingContext();
		IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);

		IndexOperations indexOps = mongoTemplate.indexOps(RecommendationEntity.class);
		resolver.resolveIndexFor(RecommendationEntity.class).forEach(e -> indexOps.ensureIndex(e));
	}
}
