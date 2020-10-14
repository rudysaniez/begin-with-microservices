package com.me.work.example.microservices.core.composite;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import com.me.work.example.microservices.core.composite.mapper.RecommendationMapper;
import com.me.work.example.microservices.core.composite.mapper.RecommendationMapperImpl;
import com.me.work.example.microservices.core.composite.mapper.ReviewMapper;
import com.me.work.example.microservices.core.composite.mapper.ReviewMapperImpl;

import lombok.Getter;
import lombok.Setter;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableConfigurationProperties(value=Application.PaginationInformation.class)
@ComponentScan(basePackages= {"com.me.work.example.microservices.core", "com.me.work.example.handler.http"})
@SpringBootApplication
public class Application {
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	/**
	 * Not reactive client, but here it's for a simple example.
	 * @return {@link RestTemplate}
	 */
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
	@Bean
	public RecommendationMapper recommendationMapper() {
		return new RecommendationMapperImpl();
	}
	
	@Bean
	public ReviewMapper reviewMapper() {
		return new ReviewMapperImpl();
	}
	
	@Getter @Setter
	@ConfigurationProperties(prefix="api.pagination")
	public static class PaginationInformation {
		
		private int pageNumber;
		private int pageSize;
	}
	
	/**
	 * <pre>
	 * http://$HOST:$PORT/api/v1/swagger-ui.html
	 * </pre>
	 * @author rudysaniez
	 */
	@Profile("product-composite-swagger")
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
	          .apis(RequestHandlerSelectors.basePackage("com.me.work.example.microservices.core.composite"))              
	          .paths(PathSelectors.any())                          
	          .build()
	          .apiInfo(new ApiInfo(title, description, version, termsOfServiceUrl, 
	        		  new Contact(contactName, contactUrl, contactEmail), 
	        		  	license, licenseUrl, java.util.Collections.emptyList()));
	    }
	}
}
