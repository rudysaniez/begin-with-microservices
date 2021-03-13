Suppliers-raw REST API connect to MongoDb Atlas
-----------------------------------------------

For the connexion with **spring-boot-starter-data-mongodb-reactive** :

- You must enable **the mongodb-atlas** profile.
- Set **spring.data.mongodb.uri** properties by *mongodb+srv://${mongo-username}:${mongo-password}@${mongo-clusters}/${spring.data.mongodb.database}?retryWrites=true&w=majority*

You can get an exception : *SSLHandshakeException : should not be presented in certificate_request*

The java fix description is [here](https://bugs.openjdk.java.net/browse/JDK-8236039)

A solution is adding the Java Option (VM Arguments) : **-Djdk.tls.client.protocols=TLSv1.2**
