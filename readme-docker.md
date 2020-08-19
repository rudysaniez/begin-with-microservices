# Java in Docker

## CPU

	echo "Runtime.getRuntime().availableProcessors()" | docker run --rm -i openjdk:12.0.2 jshell -q
	
Result, in my case :

	jshell> Runtime.getRuntime().availableProcessors()$1 ==> 4

Note :	
This command will send the *Runtime.getRuntime().availableProcessors()* string to the Docker container
that will process the string using jshell : [Java Shell tool](https://docs.oracle.com/javase/10/jshell/introduction-jshell.htm).

**Now, restrict the docker container to only be allowed to use three CPU cores :**

	echo "Runtime.getRuntime().availableProcessors()" | docker run --rm -i --cpus 3 openjdk:12.0.2 jshell -q
	
Result :

	jshell> Runtime.getRuntime().availableProcessors()$1 ==> 3
	
The JVM will respond *$1 ==> 3*.
	
**Now, limit the container to two cores. 1,024 shares correspond to one core by default :**

	echo "Runtime.getRuntime().availableProcessors()" | docker run --rm -i --cpu-shares 2048 openjdk:12.0.2 jshell -q
	
Result :

	jshell> Runtime.getRuntime().availableProcessors()$1 ==> 2
	
The JVM will respond *$1 ==> 2*. 
	

	
## Memory

	docker run --rm -it openjdk:12.0.2 java -XX:+PrintFlagsFinal -version | grep MaxHeapSize
	
Result :

	size_t MaxHeapSize                              = 522190848                                 {product} {ergonomic}
	
My JVM respond *522,190,848 bytes* which equals 498 MB.

**Now, i constrain the docker container to only use 1GB of memory** :

	docker run --rm -it -m=1024M openjdk:12.0.2 java -XX:+PrintFlagsFinal -version | grep MaxHeapSize
	
Result :

	size_t MaxHeapSize                              = 268435456                                 {product} {ergonomic}
	
The JVM will respond *268,435,456 bytes* which equals 256 MB. 256 MB is one-fourth of 1 GB, this is as expected.

**Now, i want to allow the heap to use 512 MB of the total 1 GB we have. I set -Xmx512M Java option** :

	docker run --rm -it -m=1024M openjdk:12.0.2 java -Xms512m -Xmx512m -XX:+PrintFlagsFinal -version | grep MaxHeapSize
	
Result :

	size_t MaxHeapSize                              = 536870912                                 {product} {command line}
	
The JVM will respond *536,870,912 bytes* which equals 512 MB, as expected.




