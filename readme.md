# Introduction to microservices with Spring Boot

## Presentation

4 microservices are implemented :

1. product-service (:7001)
2. recommendation-service (:7002)
3. review-service (:7003)
4. product-composite-service (:7000)
	
The product-composite-service calls :

- the product-service
- the recommendation-service
- the review-service

The **product-composite-service** aggregate the responses.

The aggregator is very simple, the objectif is to present the concept.

## Git

	git clone git@github.com:rudysaniez/basic-rest-services-2.git

## Maven

	mvn clean package -Dmaven.test.skip
	
## Start the products service

	cd product-service/target
	java -jar product-service-0.0.1-SNAPSHOT.jar
	
	curl http://localhost:7001/api/v1/products/1
	
## Start the recommendations service

	cd recommendation-service/target
	java -jar recommendation-service-0.0.1-SNAPSHOT.jar
	
	curl http://localhost:7002/api/v1/recommendations?productId=1
	
## Start the reviews service

	cd review-service/target
	java -jar review-service-0.0.1-SNAPSHOT.jar
	
	curl http://localhost:7003/api/v1/reviews?productId=1
	
## Start the products integration service

	cd product-composite-service/target
	java -jar product-composite-service-0.0.1-SNAPSHOT.jar
	
	curl http://localhost:7000/api/v1/products-composite/1
	
	
