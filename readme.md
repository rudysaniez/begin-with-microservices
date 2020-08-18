# Deploying microservices in Docker

## Presentation

4 microservices are implemented :

1. product-service
2. recommendation-service
3. review-service
4. product-composite-service
	
The **product-composite-service** calls :

- product-service
- recommendation-service
- review-service

Product-composite-service configuration file :

	---
	spring.profiles: docker
	
	server.port: 8081
	
	app:
	  product-service:
	    host: product
	    port: 8081
	    
	  recommendation-service:
	    host: recommendation
	    port: 8082
	    
	  review-service:
	    host: review
	    port: 8083
	
	logging:
	  level:
	    com.me.work.example.microservices.core.composite: DEBUG
	    com.me.work.example.handler.http: DEBUG
	    
When it receives a response, it aggregates the information.
The aggregator is very simple, the objectif is to present the concept.

## Git

	git clone git@github.com:rudysaniez/basic-rest-services-docker-3.git

## Maven

	mvn clean package
	
## Starting up the microservices landscape

	docker-compose up --build --detach
	
	docker images | grep -i basic-rest-services-docker-3
	
	docker ps -a | grep -i basic-rest-services-docker-3
	
## Call the product-composite-service

	curl http://localhost:8081/api/v1/products-composite/1
	
Response :

	{
	  "productId": "1",
	  "product": {
	    "productID": "1",
	    "name": "name-1",
	    "weight": "123"
	  },
	  "recommendations": [
	    {
	      "recommendationID": "1",
	      "productID": "1",
	      "author": "rudysaniez",
	      "rate": "1",
	      "content": "VALIDATED"
	    },
	    {
	      "recommendationID": "2",
	      "productID": "1",
	      "author": "rudysaniez",
	      "rate": "1",
	      "content": "VALIDATED"
	    },
	    {
	      "recommendationID": "3",
	      "productID": "1",
	      "author": "rudysaniez",
	      "rate": "1",
	      "content": "VALIDATED"
	    }
	  ],
	  "reviews": [
	    {
	      "reviewID": "1",
	      "productID": "1",
	      "author": "rudysaniez",
	      "subject": "review-1",
	      "content": "VALIDATED"
	    },
	    {
	      "reviewID": "2",
	      "productID": "1",
	      "author": "rudysaniez",
	      "subject": "review-2",
	      "content": "VALIDATED"
	    },
	    {
	      "reviewID": "3",
	      "productID": "1",
	      "author": "rudysaniez",
	      "subject": "review-3",
	      "content": "VALIDATED"
	    }
	  ]
	}

## Stopping up the microservices

	docker-compose down
	
	docker ps -a
	