# Developping non-blocking synchronous REST APIs using Spring

## Presentation

4 microservices are implemented :

1. products-service (products layer)
2. recommendations-service (recommendations layer)
3. reviews-service (reviews layer)
4. products-composite-service (integration and aggregation layer)

## Architecture

- Microservice core : **Products** with MongoDB
- Microservice core : **Recommendations** with MongoDB
- Microservice core : **Reviews** with MySQL
- Microservice for the aggregation layer and the integration layer : **products-composite**

## Spring Reactor

Spring 5 integrate the Project Reactor. The Project Reactor is based on *Reactive Streams specification*. The programming model is based on processing streams of data. The data types are **Mono** and **Flux**. A **Flux** is used for process a stream of *0..n* elements and a **Mono** is used for process a stream of *0..1* element.

Project Reactor is implemented in Spring 5, and for this it is necessary to use **Spring WebFlux**, **Spring WebClient** and **Spring Data** with Reactive database driver like following **mongodb-driver-reactivestreams**.

## Non-blocking persistence using Spring-Data for MongoDB

The reactive microservices **products** and **recommendations** use Spring-Data with the *ReactiveMongoRepository* features.
The CRUD methods return a **Mono** or **Flux** object.

	public Mono<Product> findByProductID(Integer productID);
	
	public Flux<Product> findByNameStartingWith(String name, Pageable page);

## Dealing with blocking code

The **Review** persistence layer use Spring-Data JPA to access its data in a relational database. In this case, we don't have support for a non-blocking programming model. We can run the blocking code using Scheduler.

	@Bean
	public Scheduler scheduler() {
		return Schedulers.fromExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
	}

**FindById** method implementation :

	@Override
	public Mono<T> findById(ID id) {
	
		return Mono.just(id).publishOn(scheduler).
		  transform(m -> m.map(repo::findById).map(Optional::get).onErrorResume(e -> Mono.empty())).
		  subscribeOn(scheduler);
	}
	  
Note : We will integrate R2DBC in a future version.

## Non-blocking REST APIs in the core services and composite services

The APIs return reactive data types : **Mono** and **Flux**. 
Services implementation don't contain any blocking code.
Tests change to use reactive services.

The APIs contract change for **products**, **recommendations**, **reviews** and **products-composite** services.Indeed the
different operations return a data type **Mono** or **Flux**.

The services implementation use a reactive persistence layer, and the composite services use **Spring WebClient** to
query the different microservices core.

## MongoDB and MySQL CLI

You can use MongoDB and MySQL CLI with **docker-compose exec**.

**Example :**

	docker-compose exec mongodb mongo productsdb --quiet --eval "db.products.find()"
	docker-compose exec mongodb mongo recommendationsdb --quiet --eval "db.recommendations.find()"
	docker-compose exec reviews-db mysql -umichael -p -e "select * from reviewsdb.REVIEW"

## Git

	git clone git@github.com:rudysaniez/begin-with-microservices.git

## Maven

	mvn clean package
	
## Starting up the microservices landscape

	docker-compose up --build --detach
	
	or
	
	./test-em-all.bash start
	
When you launch the bash named **test-em-all.bash**, several tests are launched such as product creation, recommendation and review. The container docker is always active and you can use the **products-composite** microservices.

Note : The **jq** util is required for launch this bash file.
	
Display the logs, you can launch :
	
	docker-compose logs -f
	
## Products-composite creation

	./curl-create-product-composite
	
you get this :
	
	{
		"productID": "50",
		"name": "FER A SOUDER",
		"weight": "0",
		  "recommendations": [
		    {
		      "recommendationID": "50",
		      "author": "rudysaniez",
		      "rate": "1",
		      "content": "Good product!"
		    }
		  ],
		  "reviews": [
		    {
		      "reviewID": "50",
		      "author": "rudysaniez",
		      "subject": "Nice",
		      "content": "Beautiful! it works"
		    }
		  ]
	}
	
	{
	  "productID": "51",
	  "name": "TOURNEVIS ELECTRIQUE",
	  "weight": "0",
	  "recommendations": [
	    {
	      "recommendationID": "51",
	      "author": "rudysaniez",
	      "rate": "1",
	      "content": "Good product!"
	    },
	    {
	      "recommendationID": "52",
	      "author": "nathansaniez",
	      "rate": "1",
	      "content": "Not bad!"
	    }
	  ],
	  "reviews": [
	    {
	      "reviewID": "51",
	      "author": "rudysaniez",
	      "subject": "Nice",
	      "content": "Beautiful! it works"
	    },
	    {
	      "reviewID": "52",
	      "author": "stephanesaniez",
	      "subject": "Nice",
	      "content": "Beautiful! it works"
	    },
	    {
	      "reviewID": "53",
	      "author": "nathansaniez",
	      "subject": "Not bad!",
	      "content": "My opinion : Not bad!"
	    }
	  ]
	}
		
	{
	  "productID": "52",
	  "name": "TOURNEVIS MULTI FONCTION",
	  "weight": "0",
	  "recommendations": [
	    {
	      "recommendationID": "54",
	      "author": "nathansaniez",
	      "rate": "1",
	      "content": "Not bad!"
	    },
	    {
	      "recommendationID": "53",
	      "author": "rudysaniez",
	      "rate": "1",
	      "content": "Good product!"
	    }
	  ],
	  "reviews": [
	    {
	      "reviewID": "54",
	      "author": "rudysaniez",
	      "subject": "Nice",
	      "content": "Beautiful! it works"
	    },
	    {
	      "reviewID": "55",
	      "author": "stephanesaniez",
	      "subject": "Nice",
	      "content": "Beautiful! it works"
	    },
	    {
	      "reviewID": "56",
	      "author": "nathansaniez",
	      "subject": "Not bad!",
	      "content": "My opinion : Not bad!"
	    }
	  ]
	}

## Get products-composite

	./curl-get-product-composite 50
	
you get this :

	{
	  "productID": "50",
	  "name": "FER A SOUDER",
	  "weight": "0",
	  "recommendations": {
	    "content": [
	      {
	        "recommendationID": "50",
	        "author": "rudysaniez",
	        "rate": "1",
	        "content": "Good product!"
	      }
	    ],
	    "page": {
	      "size": "5",
	      "totalElements": "1",
	      "totalPages": "1",
	      "number": "0"
	    }
	  },
	  "reviews": {
	    "content": [
	      {
	        "reviewID": "50",
	        "author": "rudysaniez",
	        "subject": "Nice",
	        "content": "Beautiful! it works"
	      }
	    ],
	    "page": {
	      "size": "5",
	      "totalElements": "1",
	      "totalPages": "1",
	      "number": "0"
	    }
	  }
	}

## Delete products-composite

	./curl-delete-product-composite 50
	
you get this :

	{
	  "timestamp": "2020-11-22T20:37:19.396714Z",
	  "path": "/products-composite/50",
	  "httpStatus": "SERVICE_UNAVAILABLE",
	  "message": "An event will be sent (Asynchronous event-driven)."
	}

## Use MongoDB and MySQL CLI TOOLS.
	
Gets products documents :
	
	docker-compose exec mongodb mongo productsdb --quiet --eval "db.products.find()"
	
Gets recommendations documents :

	docker-compose exec mongodb mongo recommendationsdb --quiet --eval "db.recommendations.find()"
	
Gets reviews in MySQL database (password is **jordan**) :

	docker-compose exec reviews-db mysql -umichael -p -e "select * from reviewsdb.REVIEW"

## Stopping up the microservices

	docker-compose down
	
## Test products-composite services

The **jq** util is required for launch this bash file.

	./test-em-all.bash start stop
	
Result :

	Wait for: http://localhost:9080/api/v1/management/info... not yet, retry #1 not yet, retry #2 Ok
	Wait the http status: 404 for curl command: curl -X GET http://localhost:9081/api/v1/products/1 -s ... Get a 404 http status, Ok
	Wait the http status: 404 for curl command: curl -X GET http://localhost:9082/api/v1/recommendations/1 -s ... Get a 404 http status, Ok
	Wait the http status: 404 for curl command: curl -X GET http://localhost:9083/api/v1/reviews/1 -s ... Get a 000 http status, , retry #1 Get a 404 http status, Ok
	
	 > Part one for the tests.
	
	 > Launch the product-composite creation : SOLAR_PANEL.
	Test OK (HTTP Code: 201), Get a 201 response status : Product-composite is created (PANNEAU_SOLAIRE).
	Test OK (HTTP Code: 200), Get a 200 response status when get a product-composite with id=1
	Test OK (actual value: 1), The productID is equals to 1.
	Test OK (actual value: SOLAR_PANEL), The product name is equals to "SOLAR_PANEL".
	Test OK (actual value: 1), 1 recommendation is found for the product-composite with id=1.
	Test OK (actual value: 1), 1 review is found for the product-composite with id=1.
