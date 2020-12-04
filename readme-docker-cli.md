Docker CLI
==========

Microservices core Product with Mongo
-------------------------------------

	docker-compose exec mongodb mongo productsdb --quiet --eval "db.products.find()"
	
- **mongodb** is the service name
- **mongo** is the command
- **productsdb** is the database name
- **db.products** is the collection name

Microservices core Recommendation with Mongo
--------------------------------------------

	docker-compose exec mongodb mongo recommendationsdb --quiet --eval "db.recommendations.find()"
	
- **mongodb** is the service name
- **mongo** is the command
- **recommendationsdb** is the database name
- **db.recommendations** is the collection name

Microservices core Review with MySQL
------------------------------------

	docker-compose exec reviews-db mysql -umichael -p -e "select * from reviewsdb.REVIEW"
	
- **reviews-db** is the service name
- **mysql** is the command
- **-umichael** is the user named Michael
- **-p** is the password, here is **jordan**
- __-e "select * from reviewsdb.REVIEW__ is the SQL request to execute
- **db.recommendations** is the collection name


Kafka topics
------------

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
