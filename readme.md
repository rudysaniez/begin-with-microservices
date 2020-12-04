Developping non-blocking synchronous REST APIs & Asynchronous Event-Driven With Spring-Boot and Spring-Cloud
================================

Presentation
------------

4 microservices are implemented :

1. product-service (non-blocking products layer)
2. recommendation-service (non-blocking recommendations layer)
3. review-service (non-blocking reviews layer)
4. product-composite-service (integration and aggregation layer)

Architecture
------------

- Microservice core *product-service* : Use **MongoDB** and consume the products topic
- Microservice core *recommendation-service* : Use **MongoDB** and consume the recommendations topic
- Microservice core *review-service* : Use **MySQL** and consume the reviews topic
- Microservice *product-composite-service* : Aggregation layer and integration layer. Event producer to *products* topic, to *recommendations* topic and to *reviews* topic.

By default, it's RabbitMQ is used and without partitions.
In this implementation, when a deletion is called, this action is perform in asynchronous. Three events are produces by
the microservice *product-composite-service* :

- One event of DELETE type is sent to products topic
- One event of DELETE type is sent to recommendations topic
- One event of DELETE type is sent to reviews topic

Spring Reactor
--------------

Spring 5 integrate the Project Reactor. The Project Reactor is based on *Reactive Streams specification*. The programming model is based on processing streams of data. The data types are **Mono** and **Flux**. A **Flux** is used for process a stream of *0..n* elements and a **Mono** is used for process a stream of *0..1* element.

Project Reactor is implemented in Spring 5, and for this it is necessary to use **Spring WebFlux**, **Spring WebClient** and **Spring Data** with Reactive database driver like following **mongodb-driver-reactivestreams**.

Non-blocking persistence using Spring-Data for MongoDB
------------------------

The non-blocking microservices **product-service** and **recommendation-service** use Spring-Data with the *ReactiveMongoRepository* features.
The CRUD methods return a **Mono** or **Flux** object.

For example :

	public Mono<Product> findByProductID(Integer productID);
	
	public Flux<Product> findByNameStartingWith(String name, Pageable page);

Dealing with blocking code
--------------------------

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
	  
**Note : We will integrate R2DBC in a future version.**

Non-blocking REST APIs in the core services and composite services
------------------------------------------------------------------

The APIs return reactive data types : **Mono** and **Flux**. 
Services implementation don't contain any blocking code.
Tests change to use reactive services.

The APIs contract change for **products**, **recommendations**, **reviews** and **products-composite** services.Indeed the
different operations return a data type **Mono** or **Flux**.

The services implementation use a reactive persistence layer, and the composite services use **Spring WebClient** to
query the different microservices core.

Git
---

	git clone git@github.com:rudysaniez/begin-with-microservices.git

Maven
-----

	mvn clean package

Starting up the microservices landscape
---------------------------------------

Using RabbitMQ without using partitions :

	docker-compose build && docker-compose up --detach
	
Using RabbitMQ with two partitions per topic :

	export COMPOSE_FILE=docker-compose-partitions.yml
	docker-compose build && docker-compose up --detach
	
Using Kafka with one partition per topic :

	export COMPOSE_FILE=docker-compose-kafka.yml
	docker-compose build && docker-compose up --detach
	
Display the logs, you can launch :
	
	docker-compose logs -f
	
	or
	
	docker-compose logs -f product-composite
	
	or
	
	docker-compose logs -f product
	
	or
	
	docker-compose logs -f recommendation
	
	or
	
	docker-compose logs -f review
	
	or
	
	docker-compose logs -f kafka
	
	or
	
	docker-compose logs -f mongodb
	
	or
	
	docker-compose logs -f reviews-db
	
you can select which logs you want to view like this.

Products-composite creation
---------------------------

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

Get products-composite
----------------------

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

Delete products-composite
-------------------------

	./curl-delete-product-composite 50
	
The deletion is done asynchronously.

You can see in logs for the **product** service when you launch :

	> Receive an event of type DELETE, created at 2020-12-04T13:12:22.355057
	
	> The product with id=50 has been deleted at 2020-12-04T13:12:22.355157

You can see in logs for the **recommendation** service when you launch :

	> Receive an event of type DELETE, created at 2020-12-04T13:12:22.358124
	
	> The recommendation(s) with productID=50 has been deleted at 2020-12-04T13:12:22.358127
	
You can see in logs for the **review** service when you launch :

	> Receive an event of type DELETE, created at 2020-12-04T13:12:22.359634
	
	> The review(s) with productID=50 has been deleted at 2020-12-04T13:12:22.359638
	
You can see in logs for the **product-composite** service when you launch :

	> A product delete event will be sent : Event(key=50, creationDate=2020-12-04T13:12:22.355057, type=DELETE)
	
	> A recommendation delete event will be sent : Event(key=50, creationDate=2020-12-04T13:12:22.358124, type=DELETE)
	
	> A review delete event will be sent : Event(key=50, creationDate=2020-12-04T13:12:22.359634, type=DELETE)

Use MongoDB and MySQL CLI TOOLS
-------------------------------
	
Gets products documents :
	
	docker-compose exec mongodb mongo productsdb --quiet --eval "db.products.find()"
	
- **mongodb** is the service name
- **mongo** is the command
- **productsdb** is the database name
- **db.products** is the collection name
	
Gets recommendations documents :

	docker-compose exec mongodb mongo recommendationsdb --quiet --eval "db.recommendations.find()"
	
- **mongodb** is the service name
- **mongo** is the command
- **recommendationsdb** is the database name
- **db.recommendations** is the collection name
	
Gets reviews in MySQL database (password is **jordan**) :

	docker-compose exec reviews-db mysql -umichael -p -e "select * from reviewsdb.REVIEW"
	
- **reviews-db** is the service name
- **mysql** is the command
- **-umichael** is the user named Michael
- **-p** is the password, here is **jordan**
- __-e "select * from reviewsdb.REVIEW__ is the SQL request to execute

RabbitMQ manager tools
----------------------

Open the following URL in web :

	http://localhost:15672/#/queues
	
You should see all queues :

- products.auditGroup
- products.productsGroup
- products.productsGroup.dlq

- recommendations.auditGroup
- recommendations.recommendationsGroup
- recommendations.recommendationsGroup.dlq

- reviews.auditGroup
- reviews.reviewsGroup
- reviews.reviewsGroup.dlq

If you use RabbitMQ with two partitions, you should see :

- products.auditGroup-0
- products.auditGroup-1
- products.productsGroup-0
- products.productsGroup-1
- products.productsGroup.dlq

- recommendations.auditGroup-0
- recommendations.auditGroup-1
- recommendations.recommendationsGroup-0
- recommendations.recommendationsGroup-1
- recommendations.recommendationsGroup.dlq

- reviews.auditGroup-0
- reviews.auditGroup-1
- reviews.reviewsGroup-0
- reviews.reviewsGroup-1
- reviews.reviewsGroup.dlq

Kafka manager tools
-------------------

To see a list of topics, run the following command :

	docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --zookeeper zookeeper --list
	
**Products topic :**

	docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --describe --zookeeper zookeeper --topic products
	
- **kafka** is the service name
- **/opt/kafka/bin/kafka-topics.sh** is the shell to execute
- **--topic products** is the topic to describe	
	
**Recommendations topic :**

	docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --describe --zookeeper zookeeper --topic recommendations
	
- **kafka** is the service name
- **/opt/kafka/bin/kafka-topics.sh** is the shell to execute
- **--topic recommendations** is the topic to describe		
	
**Reviews topic :**

	docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --describe --zookeeper zookeeper --topic reviews
	
- **kafka** is the service name
- **/opt/kafka/bin/kafka-topics.sh** is the shell to execute
- **--topic reviews** is the topic to describe

Kafka, see all the messages in a specific topic
-----------------------------------------------

**Products topic :**
	
	docker-compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic products --from-beginning --timeout-ms 1000 --partition 0

- **kafka** is the service name
- **/opt/kafka/bin/kafka-console-consumer.sh** is the shell to execute
- **--bootstrap-server localhost:9092** is the kafka broker
- **--topic products** is the topic name
- **--partition** is the partition number

**Recommendations topic :**

	docker-compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic recommendations --from-beginning --timeout-ms 1000 --partition 0

- **kafka** is the service name
- **/opt/kafka/bin/kafka-console-consumer.sh** is the shell to execute
- **--bootstrap-server localhost:9092** is the kafka broker
- **--topic recommendations** is the topic name
- **--partition** is the partition number

**Reviews topic :**

	docker-compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic reviews --from-beginning --timeout-ms 1000 --partition 0

- **kafka** is the service name
- **/opt/kafka/bin/kafka-console-consumer.sh** is the shell to execute
- **--bootstrap-server localhost:9092** is the kafka broker
- **--topic reviews** is the topic name
- **--partition** is the partition number

Stopping up the microservices
-----------------------------

	docker-compose down
	
Test products-composite services
--------------------------------

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
