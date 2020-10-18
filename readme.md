# Product composite services

## Presentation

4 microservices are implemented :

1. products-service (products layer)
2. recommendations-service (recommendations layer)
3. reviews-service (reviews layer)
4. products-composite-service (integration and aggregation layer)

## Architecture

- Microservice Product, persistence layer with MongoDB
- Microservice Recommendation, persistence layer with MongoDB
- Microservice Review, persistence layer with MySQL
- Microservice ProductComposite, integration layer (Products layer, recommendations layer and reviews layer)

You can use MongoDB and MySQL CLI with **docker-compose exec**.

**Example :**

	docker-compose exec mongodb mongo productsdb --quiet --eval "db.products.find()"
	docker-compose exec mongodb mongo recommendationsdb --quiet --eval "db.recommendations.find()"
	
	docker-compose exec reviews-db mysql -umichael -p -e "select * from reviewsdb.REVIEW"

**Note :**

For the moment, this version use a synchronous client to perform HTTP requests with the RestTemplate component.

The next version, the aim is to use a reactive client.

## products-service

	{
		"productID": 1,
		"name": "Solar panel",
		"weight": 1
	}
	
It's a simple product with name "solar panel".

You can launch a curl request like following :

	curl -X GET http://host:port/api/v1/products/1 -s | jq

## recommendations-service

	{
		"recommendationID": 1,
		"productID": 1,
		"author": "rsaniez",	
		"rate": 1,
		"content": "Very good product!!!"
	}
	
You can launch a curl request like following :

	curl -X GET http://host:port/api/v1/recommendations/1 -s | jq
	
## reviews-service

	{
		"reviewID": 1,
		"productID": 1,
		"author": "rsaniez",
		"subject": "My opinion : Very well !!!",
		"content": "This product is really useful!"
	}
	
You can launch a curl request like following :

	curl -X GET http://host:port/api/v1/reviews/1 -s | jq

## products-composite

	{
		"productID": 3,
		"name": "lance flammes",
		"weight": 0,
		"recommendations": [
			{
				"recommendationID": 3,
				"author": "rudysaniez",
				"rate": 1,
				"content": "Good product!"
			}
		],
		"reviews": [
			{
				"reviewID": 3,
				"author": "rudysaniez",
				"subject": "Nice",
				"content": "Beautiful! it works"
			}
		]
	}
	
You can launch a curl request like to following :

	curl -X GET http://host:9080/api/v1/products-composite/1 -s | jq

## Git

	git clone git@github.com:rudysaniez/begin-with-microservices.git

## Maven

	mvn clean package
	
## Starting up the microservices landscape

	docker-compose up --build --detach
	
Logs, you can use :
	
	docker-compose logs -f
	
## Call the product-composite-service

	curl -X GET http://localhost:9081/api/v1/products-composite/1 -s | jq
	
Response :

	{
		"timestamp": "2020-10-18T14:36:53.525154Z",
		"path": "/products-composite/1",
		"httpStatus": "NOT_FOUND",
		"message": "The product with productID=1 doesn't not exists."
	}

## Products creation

	curl -X POST -H "Content-Type: application/json" -d '{
		"productID": 1,
		"name": "Solar panel",
		"weight": 0,
		"recommendations": [
			{
				"recommendationID": 1,
				"author": "rudysaniez",
				"rate": 1,
				"content": "Good product!"
			}
		],
		"reviews": [
			{
				"reviewID": 1,
				"author": "rudysaniez",
				"subject": "My opinion : Very Well",
				"content": "Beautiful! it works"
			}
		]}' "http://localhost:9080/api/v1/products-composite -s | jq
		
To executed :

	curl -X POST -H "Content-Type: application/json" -d '{ "productID":1,"name":"Solar panel","weight": 0,"recommendations":[{"recommendationID": 1,"author":"rudysaniez","rate":1,"content":"Good product!"}],"reviews":[{"reviewID":1,"author":"rudysaniez","subject":"My opinion ; Very well","content":"Beautiful! it works" } ]}' "http://localhost:9080/api/v1/products-composite" -s | jq
	
Now, launch that :

	curl -X GET http://localhost:9081/api/v1/products-composite/1 -s | jq
	
Response :

	{
		"productID": "1",
		"name": "SOLAR PANEL",
		"weight": "0",
		
		"recommendations": {
		    "content": [
		      {
		        "recommendationID": "1",
		        "author": "rudysaniez",
		        "rate": "1",
		        "content": "Good product!"
		      }
		    ],
		    "page": {
		      "size": "20",
		      "totalElements": "1",
		      "totalPages": "1",
		      "number": "0"
		    }
    		},
    		
    		"reviews": {
    			"content": [
		      {
		        "reviewID": "1",
		        "author": "rudysaniez",
		        "subject": "My opinion ; Very well",
		        "content": "Beautiful! it works"
		      }
	    		],
		    "page": {
		      "size": "20",
		      "totalElements": "1",
		      "totalPages": "1",
		      "number": "0"
		    }
		}
	}
	
Congratulation, a product has been created.

You can delete it like following :

	curl -X DELETE http://localhost:9080/api/v1/products-composite/1 -s | jq

## Use MongoDB and MySQL CLI TOOLS.
	
Gets products documents :
	
	docker-compose exec mongodb mongo productsdb --quiet --eval "db.products.find()"
	
Gets recommendations documents :

	docker-compose exec mongodb mongo recommendationsdb --quiet --eval "db.recommendations.find()"
	
Gets reviews in MySQL database :

	docker-compose exec reviews-db mysql -umichael -p -e "select * from reviewsdb.REVIEW"

## Stopping up the microservices

	docker-compose down
	
## Test Product-composite-service

The **jq** util is required for launch this bash file.

	./test-em-all.bash start stop
	
Result :

	Wait for: http://localhost:9080/api/v1/management/info... not yet, retry #1 not yet, retry #2 Ok
	Wait the http status: 404 for curl command: curl -X GET http://localhost:9081/api/v1/products/1 -s ... Get a 404 http status, Ok
	Wait the http status: 404 for curl command: curl -X GET http://localhost:9082/api/v1/recommendations/1 -s ... Get a 404 http status, Ok
	Wait the http status: 404 for curl command: curl -X GET http://localhost:9083/api/v1/reviews/1 -s ... Get a 404 http status, Ok

	> Part one for the tests.

	> Launch the product-composite creation : PANNEAU_SOLAIRE.
	Test OK (HTTP Code: 201), Get a 201 response status : Product-composite is created (PANNEAU_SOLAIRE).
	Test OK (HTTP Code: 200), Get a 200 response status when get a product-composite with id=1
	Test OK (actual value: 1), The productID is equals to 1.
	Test OK (actual value: PANNEAU_SOLAIRE), The product name is equals to "PANNEAU_SOLAIRE".
	Test OK (actual value: 1), 1 recommendation is found for the product-composite with id=1.

	> Provoke a duplicate key exception.
	Test OK (HTTP Code: 422, message:  "Duplicate key : check the productID (1) or the name (panneau_solaire) of product." ), Get a 422 response status : Duplicate key exception.

	> Launch tests for get NOT_FOUND and UNPROCESSABLE_ENTITY status.
	Test OK (HTTP Code: 404, message:  "The product with productID=999 doesn't not exists." ), Get a 404 response status when the productID is equals to 999
	Test OK (HTTP Code: 422, message:  "ProductID should be greater than 0" ), Get a 422 response status when the productID is equals to 0

	> Part two for the tests.

	> Launch the deletion of product-composite with the id=1.
	Test OK (HTTP Code: 200), Get a 200 response status when deleting a product-composite with id=1 (PANNEAU_SOLAIRE)

	> Launch the creation of product-composite :  PONCEUSE
	Test OK (HTTP Code: 201), Get a 201 response status : Product-composite is created (PONCEUSE).
	Test OK (HTTP Code: 200), Get a 200 response status when get a product-composite with id=2.
	Test OK (actual value: PONCEUSE), The product name is equals to "PONCEUSE".
	Test OK (actual value: 1), 1 recommendation is found for the product-composite with id=2.
	Test OK (actual value: 1), 1 review is found for the product-composite with id=2.

	> Launch the deletion of product-composite with the id=2.
	Test OK (HTTP Code: 200), Get a 200 response status when deleting a product with id=2 (PONCEUSE).
