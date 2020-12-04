Docker CLI
==========

MySQL
-----

	docker-compose exec reviews-db mysql -umichael -p -e "select * from reviewsdb.REVIEW"
	
- *reviews-db* is the service name
- *mysql* is the command
- *-umichael* is the user named Michael
- *-p* is the password, here is jordan
- *-e* "select * from reviewsdb.REVIEW" is the SQL request to execute

Kafka topics
------------

To see a list of topics, run the following command :

	docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --zookeeper zookeeper --list

**Reviews topic :**

	docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --describe --zookeeper zookeeper --topic reviews
	
- *kafka* is the service name
- */opt/kafka/bin/kafka-topics.sh* is the shell to execute
- *--topic reviews* the topic to describe

Kafka, see all the messages in a specific topic
-----------------------------------------------

	docker-compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic reviews --from-beginning --timeout-ms 1000

- *kafka* is the service name
- */opt/kafka/bin/kafka-console-consumer.sh* is the shell to execute
- *--bootstrap-server localhost:9092* is the kafka broker
- *--topic reviews* is the topic name
