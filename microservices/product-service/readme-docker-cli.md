Docker CLI
==========

Mongo
-----

	docker-compose exec mongodb mongo productsdb --quiet --eval "db.products.find()"
	
- *mongodb* is the service name
- *mongo* is the command
- *productsdb" is the database name
- db.*products* is the collection name

Kafka topics
------------

To see a list of topics, run the following command :

	docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --zookeeper zookeeper --list
	
**Products topic :**

	docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --describe --zookeeper zookeeper --topic products
	
- *kafka* is the service name
- */opt/kafka/bin/kafka-topics.sh* is the shell to execute
- *--topic products* the topic to describe

Kafka, see all the messages in a specific topic
-----------------------------------------------

	docker-compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic products --from-beginning --timeout-ms 1000

- *kafka* is the service name
- */opt/kafka/bin/kafka-console-consumer.sh* is the shell to execute
- *--bootstrap-server localhost:9092* is the kafka broker
- *--topic products* is the topic name
