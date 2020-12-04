Docker CLI
==========

Mongo
-----

	docker-compose exec mongodb mongo recommendationsdb --quiet --eval "db.recommendations.find()"
	
- *mongodb* is the service name
- *mongo* is the command
- *recommendationsdb" is the database name
- db.*recommendations* is the collection name

Kafka topics
------------

To see a list of topics, run the following command :

	docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --zookeeper zookeeper --list
	
**Recommendations topic :**

	docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --describe --zookeeper zookeeper --topic recommendations
	
- *kafka* is the service name
- */opt/kafka/bin/kafka-topics.sh* is the shell to execute
- *--topic recommendations* the topic to describe

Kafka, see all the messages in a specific topic
-----------------------------------------------

	docker-compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic recommendations --from-beginning --timeout-ms 1000

- *kafka* is the service name
- */opt/kafka/bin/kafka-console-consumer.sh* is the shell to execute
- *--bootstrap-server localhost:9092* is the kafka broker
- *--topic recommendations* is the topic name
